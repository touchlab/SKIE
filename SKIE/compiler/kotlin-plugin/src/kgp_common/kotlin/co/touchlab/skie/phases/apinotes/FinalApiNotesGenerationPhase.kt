package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.swiftmodel.ObjCTypeRenderer
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.util.FrameworkLayout

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
