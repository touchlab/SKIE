package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.LinkPhase

object LinkObjectFilesPhase : LinkPhase {

    context(context: LinkPhase.Context)
    override suspend fun execute() {
        val additionalObjectFiles = context.objectFileProvider.allObjectFiles.map { it.absolutePath }

        context.link(additionalObjectFiles)
    }
}
