package co.touchlab.skie.api.phases.apinotes

import co.touchlab.skie.api.phases.util.ObjCTypeRenderer
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.util.FrameworkLayout
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
