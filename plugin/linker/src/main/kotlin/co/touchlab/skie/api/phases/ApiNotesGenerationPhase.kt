package co.touchlab.skie.api.phases

import co.touchlab.skie.api.apinotes.builder.ApiNotes
import co.touchlab.skie.api.apinotes.builder.ApiNotesFactory
import co.touchlab.skie.api.phases.typeconflicts.ObjCTypeRenderer
import co.touchlab.skie.plugin.api.descriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.backend.common.CommonBackendContext

class ApiNotesGenerationPhase(
    swiftModelScope: MutableSwiftModelScope,
    objCTypeRenderer: ObjCTypeRenderer,
    context: CommonBackendContext,
    private val framework: FrameworkLayout,
) : SkieLinkingPhase {

    private val apiNotesFactory = ApiNotesFactory(framework.moduleName, context.descriptorProvider, swiftModelScope, objCTypeRenderer)

    override fun execute() {
        val apiNotes = apiNotesFactory.create()

        apiNotes.createApiNotesFile()
    }

    private fun ApiNotes.createApiNotesFile() {
        val content = this.createApiNotesFileContent()

        val apiNotesFile = framework.headersDir.resolve("${framework.moduleName}.apinotes")

        apiNotesFile.writeText(content)
    }
}
