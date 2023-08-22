package co.touchlab.skie.api.phases

import co.touchlab.skie.api.phases.header.BaseHeaderInsertionPhase
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File

// Should fix warning "Umbrella header for module `X` does not include header `X-swift.h`"
class AddDummySwiftHeaderImportPhase(
    headerFile: File,
    private val framework: FrameworkLayout,
) : BaseHeaderInsertionPhase(headerFile) {

    override val insertedContent: List<String>
        get() = listOf(
            "#import \"${framework.swiftHeader.name}\"",
        )

    override fun insertImmediatelyBefore(line: String): Boolean =
        line.startsWith("NS_ASSUME_NONNULL_BEGIN")
}
