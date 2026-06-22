package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirIrFile

object ConvertSirIrFilesToSourceFilesPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.sirProvider.skieModuleFiles
            .filterIsInstance<SirIrFile>()
            .forEach {
                convertFile(it)
            }
    }

    context(context: SirPhase.Context)
    private fun convertFile(sirIrFile: SirIrFile) {
        val sourceFile = context.sirFileProvider.getGeneratedSourceFile(sirIrFile)

        sourceFile.content = SirCodeGenerator.generate(sirIrFile)
    }
}
