package co.touchlab.skie.phases.apinotes

import co.touchlab.skie.swiftmodel.ObjCTypeRenderer
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.directory.SkieBuildDirectory

class SwiftCompilationApiNotesGenerationPhase(
    swiftModelScope: MutableSwiftModelScope,
    objCTypeRenderer: ObjCTypeRenderer,
    descriptorProvider: DescriptorProvider,
    framework: FrameworkLayout,
    skieBuildDirectory: SkieBuildDirectory,
) : BaseApiNotesGenerationPhase(
    swiftModelScope,
    objCTypeRenderer,
    descriptorProvider,
    framework,
    skieBuildDirectory.swiftCompiler.apiNotes.apiNotes(framework.moduleName)
)
