package co.touchlab.skie.phases.swift

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.SwiftCompilerConfiguration
import co.touchlab.skie.configuration.SwiftCompilerConfiguration.BuildType
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCompilableFile
import co.touchlab.skie.util.Command
import java.io.File

class CompileSwiftPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val framework = context.framework
    private val cacheableKotlinFramework = context.cacheableKotlinFramework
    private val swiftCompilerConfiguration = context.swiftCompilerConfiguration
    private val rootConfiguration = context.rootConfiguration
    private val skieBuildDirectory = context.skieBuildDirectory
    private val swiftFrameworkHeader = context.skieBuildDirectory.swiftCompiler.moduleHeader(framework.moduleName)
    private val swiftFileList = context.skieBuildDirectory.swiftCompiler.config.swiftFileList(framework.moduleName)
    private val outputFileMap = context.skieBuildDirectory.swiftCompiler.config.outputFileMap
    private val objectFiles = skieBuildDirectory.swiftCompiler.objectFiles
    private val moduleDirectory = skieBuildDirectory.swiftCompiler.module
    private val targetTriple = swiftCompilerConfiguration.targetTriple

    private val isLibraryEvolutionEnabled = SkieConfigurationFlag.Build_SwiftLibraryEvolution in rootConfiguration.enabledFlags
    private val isParallelSwiftCompilationEnabled = SkieConfigurationFlag.Build_ParallelSwiftCompilation in rootConfiguration.enabledFlags
    private val isConcurrentSkieCompilationEnabled = SkieConfigurationFlag.Build_ConcurrentSkieCompilation in rootConfiguration.enabledFlags

    context(SirPhase.Context)
    override suspend fun execute() {
        val sourceFiles = sirProvider.skieModuleFiles.filterIsInstance<SirCompilableFile>().map { it.absolutePath.toFile() }
        if (sourceFiles.isEmpty()) {
            return
        }

        createSwiftFileList(sourceFiles)

        createOutputFileMap(sourceFiles)

        callSwiftCompiler()

        deleteOldObjectFiles(sourceFiles)

        copySwiftModuleFiles()

        copySwiftLibraryEvolutionFiles()
    }

    private fun createSwiftFileList(sourceFiles: List<File>) {
        val content = sourceFiles.joinToString("\n") { "'${it.absolutePath}'" }

        swiftFileList.writeText(content)
    }

    private fun createOutputFileMap(sourceFiles: List<File>) {
        if (swiftCompilerConfiguration.buildType != BuildType.Debug) {
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
        Command("${swiftCompilerConfiguration.absoluteTargetToolchainPath}/usr/bin/swiftc").apply {
            +listOf("-module-name", framework.moduleName)
            +"-import-underlying-module"
            +"-F"
            +cacheableKotlinFramework.parentDir.absolutePath
            +"-F"
            +skieBuildDirectory.swiftCompiler.fakeObjCFrameworks.directory.absolutePath
            +"-verify-emitted-module-interface"
            +"-emit-module"
            +"-emit-module-path"
            +swiftFrameworkHeader.swiftModule
            if (isLibraryEvolutionEnabled) {
                +"-enable-library-evolution"
                +"-emit-module-interface-path"
                +swiftFrameworkHeader.swiftInterface
                +"-emit-private-module-interface-path"
                +swiftFrameworkHeader.privateSwiftInterface
            }
            +"-emit-objc-header"
            +"-emit-objc-header-path"
            +swiftFrameworkHeader.swiftHeader
            getSwiftcBitcodeArg()?.let { +it }
            +"-emit-object"
            +"-parse-as-library"
            +"-enable-batch-mode"
            when (swiftCompilerConfiguration.buildType) {
                BuildType.Debug -> {
                    +"-Onone"
                    +"-incremental"
                    +"-output-file-map"
                    +outputFileMap.absolutePath
                }
                BuildType.Release -> {
                    +"-O"
                    +"-whole-module-optimization"
                }
            }
            +"-g"
            +"-module-cache-path"
            +skieBuildDirectory.cache.swiftModules.directory.absolutePath
            +"-swift-version"
            +swiftCompilerConfiguration.swiftVersion
            +parallelizationArgument
            +"-sdk"
            +swiftCompilerConfiguration.absoluteTargetSysRootPath
            +"-target"
            +swiftCompilerConfiguration.targetTriple.withOsVersion(swiftCompilerConfiguration.osVersionMin).toString()
            +swiftCompilerConfiguration.additionalFlags
            +"@${swiftFileList.absolutePath}"

            workingDirectory = objectFiles.directory

            execute(logFile = skieBuildDirectory.debug.logs.swiftc)
        }
    }

    private fun deleteOldObjectFiles(sourceFiles: List<File>) {
        when (swiftCompilerConfiguration.buildType) {
            BuildType.Debug -> {
                val sourceFilesNames = sourceFiles.map { it.nameWithoutExtension }.toSet()

                objectFiles.allFiles
                    .filterNot { it.nameWithoutExtension in sourceFilesNames }
                    .forEach {
                        it.delete()
                    }
            }
            BuildType.Release -> {
                objectFiles.allFiles
                    .filter { it.nameWithoutExtension != framework.moduleName }
                    .forEach {
                        it.delete()
                    }
            }
        }
    }

    private fun getSwiftcBitcodeArg() =
        when (swiftCompilerConfiguration.bitcodeEmbeddingMode) {
            SwiftCompilerConfiguration.BitcodeEmbeddingMode.None -> null
            SwiftCompilerConfiguration.BitcodeEmbeddingMode.Marker -> "-embed-bitcode-marker"
            SwiftCompilerConfiguration.BitcodeEmbeddingMode.Full -> "-embed-bitcode"
        }

    private fun copySwiftModuleFiles() {
        val copyFiles = mapOf(
            swiftFrameworkHeader.swiftModule to framework.swiftModule(targetTriple),
            swiftFrameworkHeader.swiftDoc to framework.swiftDoc(targetTriple),
            swiftFrameworkHeader.abiJson to framework.abiJson(targetTriple),
            swiftFrameworkHeader.swiftSourceInfo to framework.swiftSourceInfo(targetTriple),
            swiftFrameworkHeader.swiftHeader to framework.swiftHeader,
        )

        copyFiles.forEach { (source, target) ->
            source.copyTo(target, overwrite = true)
        }
    }

    private fun copySwiftLibraryEvolutionFiles() {
        if (isLibraryEvolutionEnabled) {
            swiftFrameworkHeader.swiftInterface.copyTo(framework.swiftInterface(targetTriple), overwrite = true)
            swiftFrameworkHeader.privateSwiftInterface.copyTo(framework.privateSwiftInterface(targetTriple), overwrite = true)
        } else {
            // WIP Check what happens with swiftFrameworkHeader
            framework.swiftInterface(targetTriple).delete()
            framework.privateSwiftInterface(targetTriple).delete()
        }
    }

    private val parallelizationArgument: String
        get() {
            val numberOfAvailableProcessors = if (isParallelSwiftCompilationEnabled) {
                if (isConcurrentSkieCompilationEnabled) {
                    (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)
                } else {
                    Runtime.getRuntime().availableProcessors()
                }
            } else {
                1
            }

            return "-j$numberOfAvailableProcessors"
        }
}
