package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirIrFile

object ConvertSirIrFilesToSourceFilesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.skieModuleFiles
            .filterIsInstance<SirIrFile>()
            .forEach {
                convertFile(it)
            }
    }

    context(SirPhase.Context)
    private fun convertFile(sirIrFile: SirIrFile) {
        val sourceFile = sirFileProvider.getGeneratedSourceFile(sirIrFile)

        sourceFile.content = SirCodeGenerator.generate(sirIrFile)
    }
}
