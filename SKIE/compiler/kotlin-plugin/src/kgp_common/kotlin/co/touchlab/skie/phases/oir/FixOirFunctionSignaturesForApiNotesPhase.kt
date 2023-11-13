package co.touchlab.skie.phases.oir

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.oir.element.OirSimpleFunction
import co.touchlab.skie.oir.element.OirTypeDef
import co.touchlab.skie.oir.element.OirVisibility
import co.touchlab.skie.oir.element.allFunctions
import co.touchlab.skie.oir.element.memberFunctions
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
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.swift.toValidSwiftIdentifier

// Hack for api notes (they do not support types with the same name as function parameters)
class FixOirFunctionSignaturesForApiNotesPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val typedefsMap = mutableMapOf<OirType, OirTypeDef>()

    private val typeDefsFile by lazy {
        context.oirProvider.getFile(context.oirProvider.skieModule, "TypeDefs")
    }

    context(SirPhase.Context)
    override fun execute() {
        oirProvider.allKotlinClassesAndProtocols.forEach {
            fixFunctionSignatures(it)
        }
    }

    private fun fixFunctionSignatures(oirClass: OirClass) {
        oirClass.allFunctions.forEach {
            fixFunctionSignature(it)
        }
    }

    private fun fixFunctionSignature(function: OirFunction) {
        val reservedIdentifiers = function.valueParameters.map { it.name }.toSet()

        (function as? OirSimpleFunction)?.let {
            function.returnType = function.returnType.substituteReservedIdentifiers(reservedIdentifiers)
        }

        function.valueParameters.forEach {
            it.type = it.type.substituteReservedIdentifiers(reservedIdentifiers)
        }
    }

    private fun OirType.substituteReservedIdentifiers(reservedIdentifiers: Set<String>): OirType =
        when (this) {
            is BlockPointerOirType -> copy(
                valueParameterTypes = valueParameterTypes.map { it.substituteReservedIdentifiers(reservedIdentifiers) },
                returnType = returnType.substituteReservedIdentifiers(reservedIdentifiers),
            )
            is DeclaredOirType -> {
                val typeArguments = typeArguments.map { it.substituteReservedIdentifiers(reservedIdentifiers) }

                when (val baseType = DeclaredOirType(declaration).substituteLeafType(reservedIdentifiers)) {
                    is DeclaredOirType -> baseType.copy(typeArguments = typeArguments)
                    is TypeDefOirType -> baseType.copy(typeArguments = typeArguments)
                    else -> error("Unexpected base type: $baseType")
                }
            }
            is TypeParameterUsageOirType -> this
            is SpecialReferenceOirType -> substituteLeafType(reservedIdentifiers)
            is NullableReferenceOirType -> substituteLeafType(reservedIdentifiers)
            is PointerOirType -> copy(pointee = pointee.substituteReservedIdentifiers(reservedIdentifiers))
            is PrimitiveOirType, VoidOirType -> substituteLeafType(reservedIdentifiers)
            is TypeDefOirType -> {
                (TypeDefOirType(declaration).substituteLeafType(reservedIdentifiers) as TypeDefOirType)
                    .copy(typeArguments = typeArguments.map { it.substituteReservedIdentifiers(reservedIdentifiers) })
            }
        }

    private fun OirType.substituteLeafType(reservedIdentifiers: Set<String>): OirType =
        if (this.collidesWith(reservedIdentifiers)) getOrCreateTypeDef(this).defaultType else this

    private fun getOrCreateTypeDef(type: OirType): OirTypeDef =
        typedefsMap.getOrPut(type) {
            OirTypeDef(
                name = "Skie__TypeDef__${typedefsMap.size}__" + type.renderWithoutAttributes().toValidSwiftIdentifier(),
                type = type,
                parent = typeDefsFile,
                visibility = OirVisibility.Private,
            )
        }

    private fun OirType.collidesWith(reservedIdentifiers: Set<String>): Boolean =
        when (this) {
            is DeclaredOirType -> {
                when (declaration.kind) {
                    OirClass.Kind.Class -> this.declaration.name in reservedIdentifiers
                    OirClass.Kind.Protocol -> "id" in reservedIdentifiers || this.declaration.name in reservedIdentifiers
                }
            }
            is TypeDefOirType -> this.declaration.name in reservedIdentifiers
            is NullableReferenceOirType -> this.nonNullType.collidesWith(reservedIdentifiers)
            else -> this.renderWithoutAttributes() in reservedIdentifiers
        }

    private fun OirType.renderWithoutAttributes(): String =
        this.render("", false)
}
