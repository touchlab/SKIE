package co.touchlab.skie.api.phases

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.util.Command
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.platformName
import org.jetbrains.kotlin.konan.target.withOSVersion
import java.io.File

class CompileSwiftPhase(
    private val skieContext: SkieContext,
    private val framework: FrameworkLayout,
    private val configurables: AppleConfigurables,
    private val config: KonanConfig,
) : SkieLinkingPhase {

    private val compilerConfiguration = skieContext.swiftCompilerConfiguration
    private val skieBuildDirectory = skieContext.skieBuildDirectory
    private val swiftFrameworkHeader = skieBuildDirectory.swiftCompiler.moduleHeader(framework.moduleName)

    private val targetTriple = configurables.targetTriple

    override fun execute() {
        val sourceFiles = skieBuildDirectory.swift.allSwiftFiles
        if (sourceFiles.isEmpty()) {
            return
        }

        callSwiftCompiler(sourceFiles)

        deleteOldObjectFiles(sourceFiles)

        copySwiftModuleFiles()

        addSwiftSubmoduleToModuleMap()

        addSwiftSpecificLinkerArgs()
    }

    private fun callSwiftCompiler(sourceFiles: List<File>) {
        Command("${configurables.absoluteTargetToolchain}/usr/bin/swiftc").apply {
            +listOf("-module-name", framework.moduleName)
            +"-import-underlying-module"
            +"-F"
            +skieContext.cacheableKotlinFramework.parentDir.absolutePath
            +"-F"
            +skieBuildDirectory.fakeObjCFrameworks.directory.absolutePath
            +"-emit-module"
            +"-emit-module-path"
            +swiftFrameworkHeader.swiftModule
            +"-emit-module-interface-path"
            +swiftFrameworkHeader.swiftInterface
            +"-emit-private-module-interface-path"
            +swiftFrameworkHeader.privateSwiftInterface
            +"-emit-objc-header"
            +"-emit-objc-header-path"
            +swiftFrameworkHeader.swiftHeader
            getSwiftcBitcodeArg()?.let { +it }
            +getSwiftcBuildTypeArgs()
            +"-emit-object"
            +"-enable-library-evolution"
            +"-parse-as-library"
            +"-g"
            +"-module-cache-path"
            +skieBuildDirectory.cache.swiftModules.directory.absolutePath
            +"-swift-version"
            +compilerConfiguration.swiftVersion
            +parallelizationArgument
            +"-sdk"
            +configurables.absoluteTargetSysRoot
            +"-target"
            +targetTriple.withOSVersion(configurables.osVersionMin).toString()
            +sourceFiles.map { it.absolutePath }

            workingDirectory = skieBuildDirectory.swiftCompiler.objectFiles.directory

            execute(logFile = skieBuildDirectory.debug.logs.swiftc)
        }
    }

    private fun deleteOldObjectFiles(sourceFiles: List<File>) {
        if (config.debug) {
            val sourceFilesNames = sourceFiles.map { it.nameWithoutExtension }.toSet()

            skieBuildDirectory.swiftCompiler.objectFiles.all
                .filterNot { it.nameWithoutExtension in sourceFilesNames }
                .forEach { objectFile ->
                    objectFile.delete()
                }
        } else {
            skieBuildDirectory.swiftCompiler.objectFiles.all
                .filter { it.nameWithoutExtension != framework.moduleName }
                .forEach { objectFile ->
                    objectFile.delete()
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

    private fun copySwiftModuleFiles() {
        val copyFiles = mapOf(
            swiftFrameworkHeader.swiftModule to framework.swiftModule(targetTriple),
            swiftFrameworkHeader.swiftInterface to framework.swiftInterface(targetTriple),
            swiftFrameworkHeader.privateSwiftInterface to framework.privateSwiftInterface(targetTriple),
            swiftFrameworkHeader.swiftDoc to framework.swiftDoc(targetTriple),
            swiftFrameworkHeader.abiJson to framework.abiJson(targetTriple),
            swiftFrameworkHeader.swiftSourceInfo to framework.swiftSourceInfo(targetTriple),
        )

        copyFiles.forEach { (source, target) ->
            source.copyTo(target, overwrite = true)
        }
    }

    private fun addSwiftSubmoduleToModuleMap() {
        if (!framework.swiftHeader.exists()) {
            framework.swiftHeader.writeText("")
        }

        framework.modulemapFile.appendText(
            """


            module ${framework.moduleName}.Swift {
                header "${framework.swiftHeader.name}"
                requires objc
            }
            """.trimIndent(),
        )
    }

    private fun addSwiftSpecificLinkerArgs() {
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

    private val parallelizationArgument: String
        get() = if (SkieConfigurationFlag.Build_ParallelSwiftCompilation in skieContext.skieConfiguration.enabledConfigurationFlags) {
            "-j${Runtime.getRuntime().availableProcessors()}"
        } else {
            "-j1"
        }
}
