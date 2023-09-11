package co.touchlab.skie.swiftmodel.type.translation

import co.touchlab.skie.swiftmodel.SwiftExportScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.sir.type.NonNullSirType
import org.jetbrains.kotlin.types.KotlinType

interface FlowTypeMapper {

    context(SwiftModelScope)
    fun mapType(
        type: KotlinType,
        translator: SwiftTypeTranslator,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): NonNullSirType
}
