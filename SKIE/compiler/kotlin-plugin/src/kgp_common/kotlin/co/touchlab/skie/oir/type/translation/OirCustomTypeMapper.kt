package co.touchlab.skie.oir.type.translation

import co.touchlab.skie.oir.type.NonNullReferenceOirType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.KotlinType

interface OirCustomTypeMapper {

    val mappedClassId: ClassId

    fun mapType(
        mappedSuperType: KotlinType,
        oirTypeParameterScope: OirTypeParameterScope,
    ): NonNullReferenceOirType
}
