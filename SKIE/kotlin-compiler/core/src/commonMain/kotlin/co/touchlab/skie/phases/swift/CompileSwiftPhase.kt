package co.touchlab.skie.phases.swift

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.SwiftCompilerConfiguration
import co.touchlab.skie.configuration.SwiftCompilerConfiguration.BuildType
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCompilableFile
import co.touchlab.skie.util.Command
import kotlin.io.path.absolutePathString
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

class CompileSwiftPhase(context: SirPhase.Context) : SirPhase {

    private val framework = context.framework
    private val cacheableKotlinFramework = context.cacheableKotlinFramework
    private val swiftCompilerConfiguration = context.swiftCompilerConfiguration
    private val globalConfiguration = context.globalConfiguration
    private val skieBuildDirectory = context.skieBuildDirectory
    private val swiftFrameworkHeader = context.skieBuildDirectory.swiftCompiler.moduleHeader(framework.frameworkName)
    private val swiftFileList = context.skieBuildDirectory.swiftCompiler.config.swiftFileList(framework.frameworkName)
    private val outputFileMap = context.skieBuildDirectory.swiftCompiler.config.outputFileMap
    private val objectFiles = skieBuildDirectory.swiftCompiler.objectFiles
    private val moduleDirectory = skieBuildDirectory.swiftCompiler.module
    private val objectFileProvider = context.objectFileProvider
    private val sirProvider = context.sirProvider

    private val isLibraryEvolutionEnabled = SkieConfigurationFlag.Build_SwiftLibraryEvolution in globalConfiguration.enabledFlags
    private val isParallelSwiftCompilationEnabled = SkieConfigurationFlag.Build_ParallelSwiftCompilation in globalConfiguration.enabledFlags
    private val isConcurrentSkieCompilationEnabled =
        SkieConfigurationFlag.Build_ConcurrentSkieCompilation in globalConfiguration.enabledFlags
    private val noClangModuleBreadcrumbsInStaticFramework =
        SkieConfigurationFlag.Build_NoClangModuleBreadcrumbsInStaticFramework in globalConfiguration.enabledFlags
    private val isRelativeSourcePathsInDebugSymbolsEnabled =
        SkieConfigurationFlag.Build_RelativeSourcePathsInDebugSymbols in globalConfiguration.enabledFlags

    context(SirPhase.Context)
    override suspend fun execute() {
        val compilableFiles = sirProvider.compilableFiles

        if (compilableFiles.isEmpty()) {
            return
        }

        createSwiftFileList(compilableFiles)

        createOutputFileMap(compilableFiles)

        callSwiftCompiler()
    }

    private fun createSwiftFileList(compilableFiles: List<SirCompilableFile>) {
        val content = compilableFiles.joinToString("\n") { "'${it.relativePath.pathString}'" }

        swiftFileList.writeText(content)
    }

    private fun createOutputFileMap(compilableFiles: List<SirCompilableFile>) {
        val content = when (swiftCompilerConfiguration.buildType) {
            BuildType.Debug -> getOutputFileMapContentForDebug(compilableFiles)
            BuildType.Release -> getOutputFileMapContentForRelease()
        }

        outputFileMap.writeText(content)
    }

    private fun getOutputFileMapContentForDebug(compilableFiles: List<SirCompilableFile>): String {
        val root = """
            "": {
                "emit-module-dependencies": "${moduleDirectory.dependencies(framework.frameworkName).absolutePath}",
                "swift-dependencies": "${moduleDirectory.swiftDependencies(framework.frameworkName).absolutePath}"
            },
        """.trimIndent()

        val body = compilableFiles.joinToString(",\n") { compilableFile ->
            val sourceFileName = compilableFile.relativePath.nameWithoutExtension

            """
                "${compilableFile.relativePath.pathString}": {
                    "object": "${objectFileProvider.getOrCreate(compilableFile).absolutePath.absolutePathString()}",
                    "dependencies": "${objectFiles.dependencies(sourceFileName).absolutePath}",
                    "swift-dependencies": "${objectFiles.swiftDependencies(sourceFileName).absolutePath}",
                    "swiftmodule": "${objectFiles.partialSwiftModule(sourceFileName).absolutePath}"
                }
            """.trimIndent()
        }

        return "{\n$root\n$body\n}"
    }

    private fun getOutputFileMapContentForRelease(): String {
        val objectFilePath = objectFiles.directory.resolve(framework.frameworkName + ".o").toPath()
        val objectFile = objectFileProvider.getOrCreate(objectFilePath)

        return """
            {
                "": {
                    "emit-module-dependencies": "${moduleDirectory.dependencies(framework.frameworkName).absolutePath}",
                    "swift-dependencies": "${moduleDirectory.swiftDependencies(framework.frameworkName).absolutePath}",
                    "object": "${objectFile.absolutePath.absolutePathString()}"
                }
            }
        """.trimIndent()
    }

    private fun callSwiftCompiler() {
        Command(swiftCompilerConfiguration.absoluteSwiftcPath).apply {
            +listOf("-module-name", framework.frameworkName)
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
            if (noClangModuleBreadcrumbsInStaticFramework &&
                swiftCompilerConfiguration.linkMode == SwiftCompilerConfiguration.LinkMode.Static
            ) {
                +"-Xfrontend"
                +"-no-clang-module-breadcrumbs"
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
                }
                BuildType.Release -> {
                    +"-O"
                    +"-whole-module-optimization"
                }
            }
            +"-output-file-map"
            +outputFileMap
            +"-g"
            +"-module-cache-path"
            +skieBuildDirectory.cache.swiftModules.directory
            +"-swift-version"
            +swiftCompilerConfiguration.swiftVersion
            +parallelizationArgument
            +"-sdk"
            +swiftCompilerConfiguration.absoluteTargetSysRootPath
            +"-target"
            +swiftCompilerConfiguration.targetTriple.withOsVersion(swiftCompilerConfiguration.osVersionMin).toString()

            if (isRelativeSourcePathsInDebugSymbolsEnabled) {
                +"-file-compilation-dir"
                +"."
            }

            +swiftCompilerConfiguration.freeCompilerArgs
            +"@${swiftFileList.absolutePath}"

            workingDirectory = skieBuildDirectory.swift.directory

            execute(logFile = skieBuildDirectory.debug.logs.swiftc)
        }
    }

    private fun getSwiftcBitcodeArg() = when (swiftCompilerConfiguration.bitcodeEmbeddingMode) {
        SwiftCompilerConfiguration.BitcodeEmbeddingMode.None -> null
        SwiftCompilerConfiguration.BitcodeEmbeddingMode.Marker -> "-embed-bitcode-marker"
        SwiftCompilerConfiguration.BitcodeEmbeddingMode.Full -> "-embed-bitcode"
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
