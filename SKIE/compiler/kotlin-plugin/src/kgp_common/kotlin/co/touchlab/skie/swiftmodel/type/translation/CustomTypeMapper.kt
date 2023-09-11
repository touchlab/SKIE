package co.touchlab.skie.swiftmodel.type.translation

import co.touchlab.skie.swiftmodel.SwiftExportScope
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.sir.type.NonNullSirType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.KotlinType

interface CustomTypeMapper {

    val mappedClassId: ClassId

    context(SwiftModelScope)
    fun mapType(
        mappedSuperType: KotlinType,
        translator: SwiftTypeTranslator,
        swiftExportScope: SwiftExportScope,
        flowMappingStrategy: FlowMappingStrategy,
    ): NonNullSirType
}
