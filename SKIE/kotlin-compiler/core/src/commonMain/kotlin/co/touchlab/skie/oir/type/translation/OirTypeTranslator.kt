package co.touchlab.skie.oir.type.translation

import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.DeclarationBackedKirType
import co.touchlab.skie.kir.type.DeclaredKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.NonNullReferenceKirType
import co.touchlab.skie.kir.type.NullableReferenceKirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.PointerKirType
import co.touchlab.skie.kir.type.SpecialOirKirType
import co.touchlab.skie.kir.type.TypeParameterUsageKirType
import co.touchlab.skie.kir.type.UnresolvedFlowKirType
import co.touchlab.skie.oir.element.toTypeParameterUsage
import co.touchlab.skie.oir.type.BlockPointerOirType
import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.oir.type.NonNullReferenceOirType
import co.touchlab.skie.oir.type.NullableReferenceOirType
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.PointerOirType

class OirTypeTranslator {

    fun mapType(kirType: KirType): OirType =
        when (kirType) {
            is NonNullReferenceKirType -> mapType(kirType)
            is OirBasedKirType -> kirType.oirType
            is PointerKirType -> PointerOirType(mapType(kirType.pointee), kirType.nullable)
            is NullableReferenceKirType -> NullableReferenceOirType(mapType(kirType.nonNullType), kirType.isNullableResult)
        }

    private fun mapType(kirType: NonNullReferenceKirType): NonNullReferenceOirType =
        when (kirType) {
            is BlockPointerKirType -> {
                BlockPointerOirType(
                    valueParameterTypes = kirType.valueParameterTypes.map { mapType(it) },
                    returnType = mapType(kirType.returnType),
                )
            }
            is DeclarationBackedKirType -> mapType(kirType)
            is SpecialOirKirType -> kirType.oirType
            is TypeParameterUsageKirType -> kirType.typeParameter.oirTypeParameter.toTypeParameterUsage()
        }

    fun mapType(kirType: DeclarationBackedKirType): DeclaredOirType {
        val declaredKirType = kirType.asDeclaredKirTypeOrError()

        return DeclaredOirType(
            declaration = declaredKirType.declaration.oirClass,
            typeArguments = declaredKirType.typeArguments.map { mapType(it) },
        )
    }
}
