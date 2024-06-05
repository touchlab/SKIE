package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase

object LinkObjectFilesPhase : LinkPhase {

    context(LinkPhase.Context)
    override suspend fun execute() {
        val additionalObjectFiles = objectFileProvider.allObjectFiles.map { it.absolutePath }

        link(additionalObjectFiles)
    }
}
