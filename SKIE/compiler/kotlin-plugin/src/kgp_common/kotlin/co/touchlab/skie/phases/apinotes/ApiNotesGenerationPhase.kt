package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.phases.apinotes.builder.ApiNotesFactory
import co.touchlab.skie.util.cache.writeTextIfDifferent
import java.io.File

sealed class ApiNotesGenerationPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val apiNotes = ApiNotesFactory.create()

        apiNotes.createApiNotesFile()
    }

    context(SirPhase.Context)
    private fun ApiNotes.createApiNotesFile() {
        val content = this.createApiNotesFileContent()

        getApiNotesFile().writeTextIfDifferent(content)
    }

    context(SirPhase.Context)
    protected abstract fun getApiNotesFile(): File

    object ForSwiftCompilation : ApiNotesGenerationPhase() {

        context(SirPhase.Context)
        override fun getApiNotesFile(): File =
            skieBuildDirectory.swiftCompiler.apiNotes.apiNotes(framework.moduleName)
    }

    object ForFramework : ApiNotesGenerationPhase() {

        context(SirPhase.Context)
        override fun getApiNotesFile(): File =
            framework.apiNotes
    }
}
