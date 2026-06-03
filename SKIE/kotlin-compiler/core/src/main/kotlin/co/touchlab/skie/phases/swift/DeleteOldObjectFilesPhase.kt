package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import kotlin.io.path.nameWithoutExtension

object DeleteOldObjectFilesPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        val objectFileNames = context.objectFileProvider.allObjectFiles.map { it.absolutePath.nameWithoutExtension }.toSet()

        context.skieBuildDirectory.swiftCompiler.objectFiles.allFiles
            .filterNot { it.nameWithoutExtension in objectFileNames }
            .forEach {
                it.delete()
            }
    }
}
