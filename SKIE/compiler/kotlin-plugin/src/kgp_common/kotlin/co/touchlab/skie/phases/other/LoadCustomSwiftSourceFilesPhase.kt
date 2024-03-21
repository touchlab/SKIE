package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.SirPhase

object LoadCustomSwiftSourceFilesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        skieBuildDirectory.swift.allNonGeneratedSwiftFiles.forEach {
            sirFileProvider.loadCompilableFile(it.toPath())
        }
    }
}
