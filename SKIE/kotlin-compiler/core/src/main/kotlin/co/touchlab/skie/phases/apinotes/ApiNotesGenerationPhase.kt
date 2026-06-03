package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.phases.apinotes.builder.ApiNotesFactory
import co.touchlab.skie.util.cache.writeTextIfDifferent
import java.io.File

sealed class ApiNotesGenerationPhase(
    private val exposeInternalMembers: Boolean,
) : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        val apiNotes = ApiNotesFactory(exposeInternalMembers).create()

        apiNotes.createApiNotesFile()
    }

    context(context: SirPhase.Context)
    private fun ApiNotes.createApiNotesFile() {
        val content = this.createApiNotesFileContent()

        getApiNotesFile().writeTextIfDifferent(content)
    }

    context(context: SirPhase.Context)
    protected abstract fun getApiNotesFile(): File

    object ForSwiftCompilation : ApiNotesGenerationPhase(true) {

        context(context: SirPhase.Context)
        override fun getApiNotesFile(): File =
            context.skieBuildDirectory.swiftCompiler.apiNotes.apiNotes(context.framework.frameworkName)
    }

    object ForFramework : ApiNotesGenerationPhase(false) {

        context(context: SirPhase.Context)
        override fun getApiNotesFile(): File =
            context.framework.apiNotes
    }
}
