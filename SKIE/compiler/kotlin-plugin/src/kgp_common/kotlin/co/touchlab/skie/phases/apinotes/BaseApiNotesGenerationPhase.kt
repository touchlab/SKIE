package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.phases.apinotes.builder.ApiNotes
import co.touchlab.skie.phases.apinotes.builder.ApiNotesFactory
import co.touchlab.skie.phases.SkieLinkingPhase
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.ObjCTypeRenderer
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.cache.writeTextIfDifferent
import java.io.File

abstract class BaseApiNotesGenerationPhase(
    swiftModelScope: MutableSwiftModelScope,
    objCTypeRenderer: ObjCTypeRenderer,
    descriptorProvider: DescriptorProvider,
    framework: FrameworkLayout,
    private val apiNotesFile: File,
) : SkieLinkingPhase {

    private val apiNotesFactory = ApiNotesFactory(framework.moduleName, descriptorProvider, swiftModelScope, objCTypeRenderer)

    override fun execute() {
        val apiNotes = apiNotesFactory.create()

        apiNotes.createApiNotesFile()
    }

    private fun ApiNotes.createApiNotesFile() {
        val content = this.createApiNotesFileContent()

        apiNotesFile.writeTextIfDifferent(content)
    }
}
