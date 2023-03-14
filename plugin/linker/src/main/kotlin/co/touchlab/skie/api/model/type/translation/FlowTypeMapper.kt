package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.sir.type.SwiftNonNullReferenceSirType
import org.jetbrains.kotlin.types.KotlinType

interface FlowTypeMapper {

    context(SwiftModelScope)
    fun mapType(
        type: KotlinType,
        translator: SwiftTypeTranslator,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceSirType
}
