package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase

object LoadCustomSwiftSourceFilesPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.skieBuildDirectory.swift.allNonGeneratedSwiftFiles.forEach {
            context.sirFileProvider.loadCompilableFile(it.toPath())
        }
    }
}
