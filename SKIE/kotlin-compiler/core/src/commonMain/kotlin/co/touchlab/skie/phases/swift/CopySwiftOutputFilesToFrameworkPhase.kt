package co.touchlab.skie.phases.swift

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SirPhase

class CopySwiftOutputFilesToFrameworkPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val framework = context.framework
    private val swiftFrameworkHeader = context.skieBuildDirectory.swiftCompiler.moduleHeader(framework.moduleName)
    private val targetTriple = context.swiftCompilerConfiguration.targetTriple

    context(SirPhase.Context)
    override suspend fun execute() {
        if (sirProvider.compilableFiles.isNotEmpty()) {
            copySwiftModuleFiles()
            copySwiftLibraryEvolutionFiles()
        } else {
            deleteSwiftModuleFiles()
            deleteSwiftLibraryEvolutionFiles()
        }
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

    private fun deleteSwiftModuleFiles() {
        framework.swiftModuleParent.deleteRecursively()
        framework.swiftHeader.delete()
    }

    context(SirPhase.Context)
    private fun copySwiftLibraryEvolutionFiles() {
        if (SkieConfigurationFlag.Build_SwiftLibraryEvolution.isEnabled) {
            swiftFrameworkHeader.swiftInterface.copyTo(framework.swiftInterface(targetTriple), overwrite = true)
            swiftFrameworkHeader.privateSwiftInterface.copyTo(framework.privateSwiftInterface(targetTriple), overwrite = true)
        } else {
            deleteSwiftLibraryEvolutionFiles()
        }
    }

    private fun deleteSwiftLibraryEvolutionFiles() {
        framework.swiftInterface(targetTriple).delete()
        framework.privateSwiftInterface(targetTriple).delete()
    }
}
