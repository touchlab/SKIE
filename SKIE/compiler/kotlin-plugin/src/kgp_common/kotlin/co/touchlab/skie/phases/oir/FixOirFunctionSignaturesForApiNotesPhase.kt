package co.touchlab.skie.phases.oir

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.oir.element.OirSimpleFunction
import co.touchlab.skie.oir.element.OirTypeDef
import co.touchlab.skie.oir.element.OirVisibility
import co.touchlab.skie.oir.element.functions
import co.touchlab.skie.oir.type.BlockPointerOirType
import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.oir.type.NonNullReferenceOirType
import co.touchlab.skie.oir.type.NullableReferenceOirType
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.oir.type.PointerOirType
import co.touchlab.skie.oir.type.PrimitiveOirType
import co.touchlab.skie.oir.type.SpecialReferenceOirType
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
        oirClass.functions.forEach {
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
            is NonNullReferenceOirType -> substituteReservedIdentifiers(reservedIdentifiers)
            is NullableReferenceOirType -> copy(nonNullType = nonNullType.substituteReservedIdentifiers(reservedIdentifiers))
            is PointerOirType -> copy(pointee = pointee.substituteReservedIdentifiers(reservedIdentifiers))
            is PrimitiveOirType, VoidOirType -> substituteLeafType(reservedIdentifiers)
        }

    private fun NonNullReferenceOirType.substituteReservedIdentifiers(reservedIdentifiers: Set<String>): NonNullReferenceOirType =
        when (this) {
            is BlockPointerOirType -> copy(
                valueParameterTypes = valueParameterTypes.map { it.substituteReservedIdentifiers(reservedIdentifiers) },
                returnType = returnType.substituteReservedIdentifiers(reservedIdentifiers),
            )
            is DeclaredOirType -> {
                DeclaredOirType(declaration).substituteLeafType(reservedIdentifiers)
                    .copy(
                        typeArguments = typeArguments.map { it.substituteReservedIdentifiers(reservedIdentifiers) },
                    )
            }
            is TypeParameterUsageOirType -> this
            is SpecialReferenceOirType -> substituteLeafType(reservedIdentifiers)
        }

    private fun OirType.substituteLeafType(reservedIdentifiers: Set<String>): OirType =
        if (this.renderWithoutAttributes() in reservedIdentifiers) getOrCreateTypeDef(this).defaultType else this

    private fun NonNullReferenceOirType.substituteLeafType(reservedIdentifiers: Set<String>): NonNullReferenceOirType =
        if (this.renderWithoutAttributes() in reservedIdentifiers) getOrCreateTypeDef(this).defaultType else this

    private fun DeclaredOirType.substituteLeafType(reservedIdentifiers: Set<String>): DeclaredOirType =
        if (this.renderWithoutAttributes() in reservedIdentifiers) getOrCreateTypeDef(this).defaultType else this

    private fun getOrCreateTypeDef(type: OirType): OirTypeDef =
        typedefsMap.getOrPut(type) {
            OirTypeDef(
                name = "Skie__TypeDef__${typedefsMap.size}__" + type.renderWithoutAttributes().toValidSwiftIdentifier(),
                type = type,
                parent = typeDefsFile,
                visibility = OirVisibility.Private,
            )
        }

    private fun OirType.renderWithoutAttributes(): String =
        this.render("", false)
}
