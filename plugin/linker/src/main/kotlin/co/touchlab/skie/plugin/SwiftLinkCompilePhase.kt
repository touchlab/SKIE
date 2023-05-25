package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.api.model.DescriptorBridgeProvider
import co.touchlab.skie.api.model.type.translation.BuiltinSwiftBridgeableProvider
import co.touchlab.skie.api.model.type.translation.SwiftIrDeclarationRegistry
import co.touchlab.skie.api.model.type.translation.SwiftTranslationProblemCollector
import co.touchlab.skie.api.model.type.translation.SwiftTypeTranslator
import co.touchlab.skie.api.phases.cacheableKotlinFramework
import co.touchlab.skie.api.phases.swiftCacheDirectory
import co.touchlab.skie.plugin.api.DescriptorProviderKey
import co.touchlab.skie.plugin.api.descriptorProvider
import co.touchlab.skie.plugin.api.mutableDescriptorProvider
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.util.Command
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.platformName
import org.jetbrains.kotlin.konan.target.withOSVersion
import java.io.File

class SwiftLinkCompilePhase(
    private val config: KonanConfig,
    private val context: CommonBackendContext,
    private val namer: ObjCExportNamer,
) {

    private val skieContext = context.skieContext
    private val skieModule = skieContext.module as DefaultSkieModule
    private val compilerConfiguration = skieContext.swiftCompilerConfiguration
    private val skieBuildDirectory = skieContext.skieBuildDirectory

    // TODO Refactor to phases
    fun process(): List<ObjectFile> {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return emptyList()
        }
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()
        val framework = FrameworkLayout(config.outputFile).also { it.cleanSkie() }
        val bridgeProvider = DescriptorBridgeProvider(namer)
        val swiftIrDeclarationRegistry = SwiftIrDeclarationRegistry(
            namer = namer,
        )
        val builtinSwiftBridgeableProvider = BuiltinSwiftBridgeableProvider(
            sdkPath = configurables.absoluteTargetSysRoot,
            declarationRegistry = swiftIrDeclarationRegistry,
        )
        val builtinKotlinDeclarations = BuiltinDeclarations.Kotlin(namer)

        finalizeDescriptorProvider()

        val translator = SwiftTypeTranslator(
            descriptorProvider = context.descriptorProvider,
            namer = namer,
            problemCollector = SwiftTranslationProblemCollector.Default(context),
            builtinSwiftBridgeableProvider = builtinSwiftBridgeableProvider,
            builtinKotlinDeclarations = builtinKotlinDeclarations,
            swiftIrDeclarationRegistry = swiftIrDeclarationRegistry,
        )
        val swiftModelScope = DefaultSwiftModelScope(
            namer = namer,
            descriptorProvider = context.descriptorProvider,
            bridgeProvider = bridgeProvider,
            translator = translator,
            declarationRegistry = swiftIrDeclarationRegistry,
        )

        SkieLinkingPhaseScheduler(
            skieContext = skieContext,
            skieModule = skieModule,
            context = context,
            framework = framework,
            swiftModelScope = swiftModelScope,
            builtinKotlinDeclarations = builtinKotlinDeclarations,
        ).runLinkingPhases()

        generateSwiftCode(swiftModelScope, framework)

        return compileSwiftCode(framework, configurables)
    }

    private fun generateSwiftCode(
        swiftModelScope: DefaultSwiftModelScope,
        framework: FrameworkLayout,
    ) {
        skieContext.skiePerformanceAnalyticsProducer.log("produceSwiftPoetFiles") {
            skieModule.produceSwiftPoetFiles(swiftModelScope, framework.moduleName)
                .forEach { fileSpec ->
                    val file = skieBuildDirectory.swift.generated.swiftFile(fileSpec.name)

                    file.writeText(fileSpec.toString())
                }
        }

        skieContext.skiePerformanceAnalyticsProducer.log("produceTextFiles") {
            skieModule.produceTextFiles()
                .forEach { textFile ->
                    val file = skieBuildDirectory.swift.generated.swiftFile(textFile.name)

                    file.writeText(textFile.content)
                }
        }
    }

    private fun finalizeDescriptorProvider() {
        val finalizedDescriptorProvider = context.mutableDescriptorProvider.preventFurtherMutations()
        context.configuration.put(DescriptorProviderKey, finalizedDescriptorProvider)
    }

    private fun compileSwiftCode(
        framework: FrameworkLayout,
        configurables: AppleConfigurables,
    ): List<ObjectFile> {
        val sourceFiles = skieBuildDirectory.swift.allSwiftFiles

        val swiftObjectPaths = if (sourceFiles.isNotEmpty()) {
            val compileDirectory = SwiftCompileDirectory(framework.moduleName, config.tempFiles.create("swift-object"))

            callSwiftCompiler(configurables, framework, sourceFiles, compileDirectory)

            copySwiftModuleFiles(configurables, compileDirectory, framework)

            addSwiftSubmoduleToModuleMap(framework)

            addSwiftSpecificLinkerArgs(configurables)

            compileDirectory.objectFiles()
        } else {
            emptyList()
        }

        return swiftObjectPaths
    }

    private fun callSwiftCompiler(
        configurables: AppleConfigurables,
        framework: FrameworkLayout,
        sourceFiles: List<File>,
        compileDirectory: SwiftCompileDirectory,
    ) {
        val targetTriple = configurables.targetTriple

        Command("${configurables.absoluteTargetToolchain}/usr/bin/swiftc").apply {
            +listOf("-module-name", framework.moduleName)
            +"-import-underlying-module"
            +"-F"
            +skieContext.cacheableKotlinFramework.parentDir.absolutePath
            +"-emit-module"
            +"-emit-module-path"
            +compileDirectory.swiftModule
            +"-emit-module-interface-path"
            +compileDirectory.swiftInterface
            +"-emit-private-module-interface-path"
            +compileDirectory.privateSwiftInterface
            +"-emit-objc-header"
            +"-emit-objc-header-path"
            +compileDirectory.swiftHeader
            getSwiftcBitcodeArg()?.let { +it }
            +getSwiftcBuildTypeArgs()
            +"-emit-object"
            +"-enable-library-evolution"
            +"-parse-as-library"
            +"-g"
            +"-module-cache-path"
            +skieContext.swiftCacheDirectory.absolutePath
            +"-swift-version"
            +compilerConfiguration.swiftVersion
            +parallelizationArgument()
            // +"-use-frontend-parseable-output"
            +"-sdk"
            +configurables.absoluteTargetSysRoot
            +"-target"
            +targetTriple.withOSVersion(configurables.osVersionMin).toString()
            +sourceFiles.map { it.absolutePath }

            workingDirectory = compileDirectory.workingDirectory

            skieContext.skiePerformanceAnalyticsProducer.log("compileSwift") {
                execute(logFile = skieBuildDirectory.debug.logs.swiftc)
            }
        }
    }

    private fun getSwiftcBitcodeArg() = when (config.configuration.get(KonanConfigKeys.BITCODE_EMBEDDING_MODE)) {
        BitcodeEmbedding.Mode.NONE, null -> null
        BitcodeEmbedding.Mode.FULL -> "-embed-bitcode"
        BitcodeEmbedding.Mode.MARKER -> "-embed-bitcode-marker"
    }

    private fun getSwiftcBuildTypeArgs() = if (config.debug) {
        emptyList()
    } else {
        listOf("-O", "-whole-module-optimization")
    }

    private fun copySwiftModuleFiles(
        configurables: AppleConfigurables,
        compileDirectory: SwiftCompileDirectory,
        framework: FrameworkLayout,
    ) {
        val targetTriple = configurables.targetTriple

        val copyFiles = mapOf(
            compileDirectory.swiftModule to framework.swiftModule(targetTriple),
            compileDirectory.swiftInterface to framework.swiftInterface(targetTriple),
            compileDirectory.privateSwiftInterface to framework.privateSwiftInterface(targetTriple),
            compileDirectory.swiftDoc to framework.swiftDoc(targetTriple),
            compileDirectory.abiJson to framework.abiJson(targetTriple),
            compileDirectory.swiftSourceInfo to framework.swiftSourceInfo(targetTriple),
        )

        copyFiles.forEach { (source, target) ->
            source.copyTo(target, overwrite = true)
        }
    }

    private fun addSwiftSubmoduleToModuleMap(framework: FrameworkLayout) {
        if (!framework.swiftHeader.exists()) {
            framework.swiftHeader.writeText("")
        }
        framework.modulemapFile.appendText("""


            module ${framework.moduleName}.Swift {
                header "${framework.swiftHeader.name}"
                requires objc
            }
            """.trimIndent(),
        )
    }

    private fun addSwiftSpecificLinkerArgs(configurables: AppleConfigurables) {
        val swiftLibSearchPaths = listOf(
            File(configurables.absoluteTargetToolchain, "usr/lib/swift/${configurables.platformName().lowercase()}"),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }
        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift", "-dead_strip",
        )

        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)
    }

    private fun parallelizationArgument(): String {
        if (compilerConfiguration.parallelCompilation) {
            return "-j${Runtime.getRuntime().availableProcessors()}"
        } else {
            return "-j1"
        }
    }
}
