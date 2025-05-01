package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.phases.apinotes.builder.ApiNotesFactory
import co.touchlab.skie.util.cache.writeTextIfDifferent
import java.io.File

sealed class ApiNotesGenerationPhase(private val exposeInternalMembers: Boolean) : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val apiNotes = ApiNotesFactory(exposeInternalMembers).create()

        apiNotes.createApiNotesFile()
    }

    context(SirPhase.Context)
    private fun ApiNotes.createApiNotesFile() {
        val content = this.createApiNotesFileContent()

        getApiNotesFile().writeTextIfDifferent(content)
    }

    context(SirPhase.Context)
    protected abstract fun getApiNotesFile(): File

    object ForSwiftCompilation : ApiNotesGenerationPhase(true) {

        context(SirPhase.Context)
        override fun getApiNotesFile(): File = skieBuildDirectory.swiftCompiler.apiNotes.apiNotes(framework.frameworkName)
    }

    object ForFramework : ApiNotesGenerationPhase(false) {

        context(SirPhase.Context)
        override fun getApiNotesFile(): File = framework.apiNotes
    }
}
