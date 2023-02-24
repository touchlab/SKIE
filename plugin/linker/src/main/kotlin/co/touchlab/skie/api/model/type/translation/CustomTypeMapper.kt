package co.touchlab.skie.api.model.type.translation

import co.touchlab.skie.plugin.api.model.SwiftExportScope
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.type.translation.SwiftNonNullReferenceTypeModel
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.KotlinType

interface CustomTypeMapper {

    val isTypeSubstitution: Boolean
        get() = false

    val mappedClassId: ClassId

    context(SwiftModelScope)
    fun mapType(
        mappedSuperType: KotlinType,
        translator: SwiftTypeTranslator,
        swiftExportScope: SwiftExportScope,
    ): SwiftNonNullReferenceTypeModel
}
