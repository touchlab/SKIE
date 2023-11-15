package co.touchlab.skie.sir.type.translation

import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.oir.type.BlockPointerOirType
import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.oir.type.NullableReferenceOirType
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.PointerOirType
import co.touchlab.skie.oir.type.PrimitiveOirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
import co.touchlab.skie.oir.type.TypeDefOirType
import co.touchlab.skie.oir.type.TypeParameterUsageOirType
import co.touchlab.skie.oir.type.VoidOirType
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.type.LambdaSirType
import co.touchlab.skie.sir.type.NonNullSirType
import co.touchlab.skie.sir.type.NullableSirType
import co.touchlab.skie.sir.type.OirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SkieErrorSirType
import co.touchlab.skie.sir.type.SpecialSirType
import co.touchlab.skie.sir.type.TypeParameterUsageSirType
import co.touchlab.skie.sir.type.toNullable

class SirTypeTranslator(
    private val sirBuiltins: SirBuiltins,
) {

    fun mapType(oirType: OirType, isEscaping: Boolean = false): SirType =
        when (oirType) {
            is PointerOirType -> mapType(oirType)
            is PrimitiveOirType -> mapType(oirType)
            is BlockPointerOirType -> mapType(oirType, isEscaping)
            is DeclaredOirType -> mapType(oirType)
            is TypeDefOirType -> mapType(oirType, isEscaping)
            is SpecialReferenceOirType -> mapType(oirType)
            is TypeParameterUsageOirType -> mapType(oirType)
            is NullableReferenceOirType -> mapType(oirType)
            VoidOirType -> sirBuiltins.Swift.Void.defaultType
        }

    fun mapReturnType(oirType: OirType, errorHandlingStrategy: OirFunction.ErrorHandlingStrategy): SirType =
        when (errorHandlingStrategy) {
            OirFunction.ErrorHandlingStrategy.Crashes -> mapType(oirType)
            OirFunction.ErrorHandlingStrategy.ReturnsBoolean -> sirBuiltins.Swift.Void.defaultType
            OirFunction.ErrorHandlingStrategy.SetsErrorOut -> mapType(oirType)
            OirFunction.ErrorHandlingStrategy.ReturnsZero -> when (val type = mapType(oirType)) {
                is NullableSirType -> type.type
                is NonNullSirType -> type
            }
        }

    fun mapSuspendCompletionType(oirType: OirType): SirType {
        require(oirType is BlockPointerOirType) { "Suspend completion must be a BlockPointerOirType. Was: $oirType" }

        return when (oirType.valueParameterTypes.size) {
            1 -> sirBuiltins.Swift.Void.defaultType
            2 -> when (val resultType = oirType.valueParameterTypes.first()) {
                is NullableReferenceOirType -> if (resultType.isNullableResult) {
                    mapType(resultType)
                } else {
                    mapType(resultType.nonNullType)
                }
                else -> error("Suspend completion result type must be a NullableReferenceOirType. Was: $resultType")
            }
            else -> error("Suspend completion must have 1 or 2 parameters. Was: ${oirType.valueParameterTypes.size}")
        }
    }

    private fun mapType(oirType: PointerOirType): SirType =
        sirBuiltins.Swift.UnsafeMutableRawPointer.defaultType.toNullable(oirType.nullable)

    private fun mapType(oirType: PrimitiveOirType): SirType =
        when (oirType) {
            PrimitiveOirType.unichar -> sirBuiltins.Foundation.unichar.defaultType
            PrimitiveOirType.BOOL -> sirBuiltins.Swift.Bool.defaultType
            PrimitiveOirType.double -> sirBuiltins.Swift.Double.defaultType
            PrimitiveOirType.float -> sirBuiltins.Swift.Float.defaultType
            PrimitiveOirType.int8_t -> sirBuiltins.Swift.Int8.defaultType
            PrimitiveOirType.int16_t -> sirBuiltins.Swift.Int16.defaultType
            PrimitiveOirType.int32_t -> sirBuiltins.Swift.Int32.defaultType
            PrimitiveOirType.int64_t -> sirBuiltins.Swift.Int64.defaultType
            PrimitiveOirType.uint8_t -> sirBuiltins.Swift.UInt8.defaultType
            PrimitiveOirType.uint16_t -> sirBuiltins.Swift.UInt16.defaultType
            PrimitiveOirType.uint32_t -> sirBuiltins.Swift.UInt32.defaultType
            PrimitiveOirType.uint64_t -> sirBuiltins.Swift.UInt64.defaultType
            PrimitiveOirType.NSUInteger -> sirBuiltins.Swift.UInt.defaultType
        }

    private fun mapType(
        oirType: BlockPointerOirType,
        isEscaping: Boolean,
    ): SirType =
        LambdaSirType(
            valueParameterTypes = oirType.valueParameterTypes.map { mapType(it, isEscaping = true) },
            returnType = mapType(oirType.returnType, isEscaping = false),
            isEscaping = isEscaping,
        )

    private fun mapType(oirType: DeclaredOirType): SirType =
        OirDeclaredSirType(
            declaration = oirType.declaration,
            typeArguments = oirType.typeArguments,
            mapTypeArgument = ::mapTypeArgument,
        )

    private fun mapType(
        oirType: TypeDefOirType,
        isEscaping: Boolean,
    ): SirType =
        mapType(oirType.declaration.type, isEscaping = isEscaping)

    private fun mapType(oirType: SpecialReferenceOirType): SirType =
        when (oirType) {
            SpecialReferenceOirType.Class -> sirBuiltins.Swift.AnyClass.defaultType
            SpecialReferenceOirType.Id -> SpecialSirType.Any
            SpecialReferenceOirType.InstanceType -> SpecialSirType.Self
            SpecialReferenceOirType.Protocol -> SpecialSirType.Protocol
        }

    private fun mapType(oirType: TypeParameterUsageOirType): SirType =
        oirType.typeParameter.sirTypeParameter?.let { TypeParameterUsageSirType(it) } ?: sirBuiltins.Swift.AnyObject.defaultType

    private fun mapType(oirType: NullableReferenceOirType): SirType =
        mapType(oirType.nonNullType, isEscaping = false).toNullable()

    private fun mapTypeArgument(typeArgument: OirType, typeParameter: SirTypeParameter): SirType {
        if (typeArgument is BlockPointerOirType) {
            return SkieErrorSirType.Lambda
        }

        var result = mapType(typeArgument)

        val mustBeHashableType = typeParameter.bounds.any { it.asHashableType() != null }
        if (mustBeHashableType) {
            result = result.asHashableType() ?: sirBuiltins.Swift.AnyHashable.defaultType
        }

        val mustBeReferenceType = typeParameter.bounds.any { it.asReferenceType() != null }
        if (mustBeReferenceType) {
            result = result.asReferenceType() ?: sirBuiltins.Swift.AnyObject.defaultType
        }

        return result
    }
}
