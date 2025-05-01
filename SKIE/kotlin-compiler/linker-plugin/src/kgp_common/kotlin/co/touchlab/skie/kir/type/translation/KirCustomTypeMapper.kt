package co.touchlab.skie.kir.type.translation

import co.touchlab.skie.kir.type.NonNullReferenceKirType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.KotlinType

interface KirCustomTypeMapper {

    val mappedClassId: ClassId

    context(KirTypeParameterScope)
    fun mapType(mappedSuperType: KotlinType): NonNullReferenceKirType
}
