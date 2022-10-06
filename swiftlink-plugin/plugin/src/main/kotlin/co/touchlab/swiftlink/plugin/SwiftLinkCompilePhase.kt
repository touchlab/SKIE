package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.resolve.DefaultTemplateVariableResolver
import co.touchlab.swiftlink.plugin.resolve.KotlinSymbolRegistry
import co.touchlab.swiftlink.plugin.resolve.KotlinSymbolResolver
import co.touchlab.swiftlink.plugin.transform.ApiNotes
import co.touchlab.swiftlink.plugin.transform.ApiTransformResolver
import co.touchlab.swiftlink.plugin.transform.BridgedName
import co.touchlab.swiftpack.spec.module.SwiftPackModule
import co.touchlab.swiftpack.spi.produceSwiftFile
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec
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
import org.jetbrains.kotlin.library.impl.javaFile
import java.io.File

class SwiftLinkCompilePhase(
    private val config: KonanConfig,
    private val context: CommonBackendContext,
    private val namer: ObjCExportNamer,
    private val swiftSourceFiles: List<File>,
    private val expandedSwiftDir: File,
) {
    private val moduleLoader = SwiftPackModuleLoader(context.configuration)

    private val SwiftPackModule.Name.sourceFilePrefix: String
        get() = when (this) {
            is SwiftPackModule.Name.Simple -> name
            is SwiftPackModule.Name.Namespaced -> "${namespace}_${name.sourceFilePrefix}"
        }

    fun process(): List<ObjectFile> {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return emptyList()
        }

        val symbolRegistry = KotlinSymbolRegistry(moduleLoader.references)
        val symbolResolver = KotlinSymbolResolver(context, symbolRegistry)
        val transformResolver = ApiTransformResolver(namer, symbolResolver, moduleLoader.transforms)
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()
        val swiftSourcesDir = expandedSwiftDir.also {
            it.deleteRecursively()
            it.mkdirs()
        }

        val bridgeTypeAliasesFile = createBridgeTypeAliasesFileIfNeeded(transformResolver, swiftSourcesDir)

        val sourceFiles = listOfNotNull(bridgeTypeAliasesFile) +
            produceSwiftFilesFromModules(symbolResolver, transformResolver, swiftSourcesDir) + swiftSourceFiles

        val framework = FrameworkLayout(config.outputFile)
        val apiNotes = ApiNotes(framework.moduleName, transformResolver)

        val swiftObjectPaths = if (sourceFiles.isNotEmpty()) {
            apiNotes.save(framework.headersDir, false)

            val swiftObjectsDir = config.tempFiles.create("swift-object").also { it.mkdirs() }

            compileSwift(configurables, framework, sourceFiles, swiftObjectsDir.javaFile())

            appendSwiftHeaderImportIfNeeded(framework)

            addSwiftSpecificLinkerArgs(configurables)

            swiftObjectsDir.listFilesOrEmpty.filter { it.extension == "o" }.map { it.absolutePath }
        } else {
            emptyList()
        }

        apiNotes.save(framework.headersDir, true)

        disableWildcardExportIfNeeded(framework)

        return swiftObjectPaths
    }

    private fun createBridgeTypeAliasesFileIfNeeded(
        transformResolver: ApiTransformResolver,
        swiftSourcesDir: File,
    ): File? {
        val bridgeTypealiases = transformResolver.typeTransforms.mapNotNull { typeTransform ->
            val bridgedName = typeTransform.bridgedName as? BridgedName.Relative ?: return@mapNotNull null
            TypeAliasSpec.builder(bridgedName.typealiasName, DeclaredTypeName.qualifiedTypeName(".${bridgedName.typealiasValue}"))
                .addModifiers(Modifier.PUBLIC)
                .build()
        }

        if (bridgeTypealiases.isEmpty()) {
            return null
        }

        val file = swiftSourcesDir.resolve("__ObjCBridgeTypeAliases.swift")
        FileSpec.builder("__ObjCBridgeTypeAliases")
            .apply {
                bridgeTypealiases.forEach { addType(it) }
            }
            .build()
            .toString()
            .also { file.writeText(it) }

        return file
    }

    private fun produceSwiftFilesFromModules(
        symbolResolver: KotlinSymbolResolver,
        transformResolver: ApiTransformResolver,
        swiftSourcesDir: File,
    ) = moduleLoader.modules.flatMap { module ->
        val filenamePrefix = module.name.sourceFilePrefix
        val templateVariableResolver = DefaultTemplateVariableResolver(
            config.moduleId,
            namer,
            symbolResolver,
            transformResolver,
            module.templateVariables,
        )

        module.files.map { file ->
            val finalContents = file.produceSwiftFile(templateVariableResolver)
            val targetSwiftFile = swiftSourcesDir.resolve("${filenamePrefix}_${file.name}.swift")
            targetSwiftFile.writeText(finalContents)
            targetSwiftFile
        }
    }

    private fun compileSwift(configurables: AppleConfigurables, framework: FrameworkLayout, sourceFiles: List<File>, swiftObjectsDir: File) {
        val targetTriple = configurables.targetTriple

        Command("${configurables.absoluteTargetToolchain}/usr/bin/swiftc").apply {
            +"-v"
            +listOf("-module-name", framework.moduleName)
            +"-import-underlying-module"
            +listOf("-Xcc", "-fmodule-map-file=$framework.modulemapFile")
            +"-emit-module-interface-path"
            +framework.swiftModule.resolve("$targetTriple.swiftinterface").absolutePath
            +"-emit-module-path"
            +framework.swiftModule.resolve("$targetTriple.swiftmodule").absolutePath
            +"-emit-objc-header-path"
            +framework.swiftHeader.absolutePath
            getSwiftcBitcodeArg()?.let { +it }
            +getSwiftcBuildTypeArgs()
            +"-emit-object"
            +"-enable-library-evolution"
            +"-parse-as-library"
            +"-g"
            +"-sdk"
            +configurables.absoluteTargetSysRoot
            +"-target"
            +targetTriple.withOSVersion(configurables.osVersionMin).toString()
            +sourceFiles.map { it.absolutePath }

            workingDirectory = swiftObjectsDir

            execute()
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

    private fun appendSwiftHeaderImportIfNeeded(framework: FrameworkLayout) {
        if (framework.swiftHeader.exists()) {
            // TODO: This line seems to fix the "warning: umbrella header for module 'ExampleKit' does not include header 'ExampleKit-Swift.h'",
            // TODO: but not in all invocations, which is why it needs to be investigated (for example running link for both dynamic and static).
            framework.kotlinHeader.appendText("\n#import \"${framework.swiftHeader.name}\"\n")
        }
    }

    private fun addSwiftSpecificLinkerArgs(configurables: AppleConfigurables) {
        val swiftLibSearchPaths = listOf(
            File(configurables.absoluteTargetToolchain, "usr/lib/swift/${configurables.platformName().lowercase()}"),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }
        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift"
        )

        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)
    }

    private fun disableWildcardExportIfNeeded(framework: FrameworkLayout) {
        if (config.configuration.getBoolean(ConfigurationKeys.disableWildcardExport)) {
            framework.modulemapFile.writeText(
                framework.modulemapFile.readLines().filterNot { it.contains("export *") }.joinToString(System.lineSeparator())
            )
        }
    }
}
