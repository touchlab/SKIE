package co.touchlab.skie.phases.swift

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.Command
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.konan.target.platformName
import org.jetbrains.kotlin.konan.target.withOSVersion
import java.io.File

class CompileSwiftPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val framework = context.framework
    private val cacheableKotlinFramework = context.cacheableKotlinFramework
    private val konanConfig = context.konanConfig
    private val swiftCompilerConfiguration = context.swiftCompilerConfiguration
    private val skieConfiguration = context.skieConfiguration
    private val configurables = context.configurables
    private val skieBuildDirectory = context.skieBuildDirectory
    private val targetTriple = context.configurables.targetTriple
    private val swiftFrameworkHeader = context.skieBuildDirectory.swiftCompiler.moduleHeader(framework.moduleName)
    private val swiftFileList = context.skieBuildDirectory.swiftCompiler.config.swiftFileList(framework.moduleName)
    private val outputFileMap = context.skieBuildDirectory.swiftCompiler.config.outputFileMap
    private val objectFiles = skieBuildDirectory.swiftCompiler.objectFiles
    private val moduleDirectory = skieBuildDirectory.swiftCompiler.module

    private val isDebug = konanConfig.debug

    context(SirPhase.Context)
    override fun execute() {
        // WIP Replace with load written files phase
        val sourceFiles = skieBuildDirectory.swift.allSwiftFiles
        if (sourceFiles.isEmpty()) {
            return
        }

        createSwiftFileList(sourceFiles)

        createOutputFileMap(sourceFiles)

        callSwiftCompiler()

        deleteOldObjectFiles(sourceFiles)

        copySwiftModuleFiles()

        addSwiftSubmoduleToModuleMap()

        addSwiftSpecificLinkerArgs()
    }

    private fun createSwiftFileList(sourceFiles: List<File>) {
        val content = sourceFiles.joinToString("\n") { "'${it.absolutePath}'" }

        swiftFileList.writeText(content)
    }

    private fun createOutputFileMap(sourceFiles: List<File>) {
        if (!isDebug) {
            return
        }

        val root = """
              "": {
                "emit-module-dependencies": "${moduleDirectory.dependencies(framework.moduleName).absolutePath}",
                "swift-dependencies": "${moduleDirectory.swiftDependencies(framework.moduleName).absolutePath}"
              },
        """.trimIndent()

        val body = sourceFiles.joinToString(",\n") { sourceFile ->
            val sourceFileName = sourceFile.nameWithoutExtension

            """
                "${sourceFile.absolutePath}": {
                    "object": "${objectFiles.objectFile(sourceFileName).absolutePath}",
                    "dependencies": "${objectFiles.dependencies(sourceFileName).absolutePath}",
                    "swift-dependencies": "${objectFiles.swiftDependencies(sourceFileName).absolutePath}",
                    "swiftmodule": "${objectFiles.partialSwiftModule(sourceFileName).absolutePath}"
                }
            """.trimIndent()
        }

        val content = "{\n$root\n$body\n}"

        outputFileMap.writeText(content)
    }

    private fun callSwiftCompiler() {
        Command("${configurables.absoluteTargetToolchain}/usr/bin/swiftc").apply {
            +listOf("-module-name", framework.moduleName)
            +"-import-underlying-module"
            +"-F"
            +cacheableKotlinFramework.parentDir.absolutePath
            +"-F"
            +skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.directory.absolutePath
            +"-enable-library-evolution"
            +"-verify-emitted-module-interface"
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
            +"-emit-object"
            +"-parse-as-library"
            +"-enable-batch-mode"
            if (isDebug) {
                +"-Onone"
                +"-incremental"
                +"-output-file-map"
                +outputFileMap.absolutePath
            } else {
                +"-O"
                +"-whole-module-optimization"
            }
            +"-g"
            +"-module-cache-path"
            +skieBuildDirectory.cache.swiftModules.directory.absolutePath
            +"-swift-version"
            +swiftCompilerConfiguration.swiftVersion
            +parallelizationArgument
            +"-sdk"
            +configurables.absoluteTargetSysRoot
            +"-target"
            +configurables.targetTriple.withOSVersion(configurables.osVersionMin).toString()
            +"@${swiftFileList.absolutePath}"

            workingDirectory = objectFiles.directory

            execute(logFile = skieBuildDirectory.debug.logs.swiftc)
        }
    }

    private fun deleteOldObjectFiles(sourceFiles: List<File>) {
        if (isDebug) {
            val sourceFilesNames = sourceFiles.map { it.nameWithoutExtension }.toSet()

            objectFiles.allFiles
                .filterNot { it.nameWithoutExtension in sourceFilesNames }
                .forEach {
                    it.delete()
                }
        } else {
            objectFiles.allFiles
                .filter { it.nameWithoutExtension != framework.moduleName }
                .forEach {
                    it.delete()
                }
        }
    }

    private fun getSwiftcBitcodeArg() =
        when (konanConfig.configuration.get(KonanConfigKeys.BITCODE_EMBEDDING_MODE)) {
            BitcodeEmbedding.Mode.NONE, null -> null
            BitcodeEmbedding.Mode.FULL -> "-embed-bitcode"
            BitcodeEmbedding.Mode.MARKER -> "-embed-bitcode-marker"
        }

    private fun copySwiftModuleFiles() {
        val copyFiles = mapOf(
            swiftFrameworkHeader.swiftModule to framework.swiftModule(targetTriple),
            swiftFrameworkHeader.swiftInterface to framework.swiftInterface(targetTriple),
            swiftFrameworkHeader.privateSwiftInterface to framework.privateSwiftInterface(targetTriple),
            swiftFrameworkHeader.swiftDoc to framework.swiftDoc(targetTriple),
            swiftFrameworkHeader.abiJson to framework.abiJson(targetTriple),
            swiftFrameworkHeader.swiftSourceInfo to framework.swiftSourceInfo(targetTriple),
            swiftFrameworkHeader.swiftHeader to framework.swiftHeader,
        )

        copyFiles.forEach { (source, target) ->
            source.copyTo(target, overwrite = true)
        }
    }

    private fun addSwiftSubmoduleToModuleMap() {
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

        konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        konanConfig.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)
    }

    private val parallelizationArgument: String
        get() = if (SkieConfigurationFlag.Build_ParallelSwiftCompilation in skieConfiguration.enabledConfigurationFlags) {
            "-j${Runtime.getRuntime().availableProcessors()}"
        } else {
            "-j1"
        }
}
