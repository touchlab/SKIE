package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase

object LinkObjectFilesPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        val additionalObjectFiles = skieBuildDirectory.swiftCompiler.objectFiles.allObjectFiles.map { it.toPath() }

        link(additionalObjectFiles)
    }
}
