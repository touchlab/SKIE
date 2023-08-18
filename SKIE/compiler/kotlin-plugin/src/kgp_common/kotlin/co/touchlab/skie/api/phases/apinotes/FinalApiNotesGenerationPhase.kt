package co.touchlab.skie.api.phases.apinotes

import co.touchlab.skie.api.phases.util.ObjCTypeRenderer
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.util.FrameworkLayout

class FinalApiNotesGenerationPhase(
    swiftModelScope: MutableSwiftModelScope,
    objCTypeRenderer: ObjCTypeRenderer,
    descriptorProvider: DescriptorProvider,
    framework: FrameworkLayout,
) : BaseApiNotesGenerationPhase(
    swiftModelScope,
    objCTypeRenderer,
    descriptorProvider,
    framework,
    framework.apiNotes,
)
