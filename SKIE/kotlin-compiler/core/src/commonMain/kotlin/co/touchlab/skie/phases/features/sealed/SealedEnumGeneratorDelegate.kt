package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
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
import co.touchlab.skie.phases.util.MustBeExecutedAfterBridgingConfiguration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirEnumCaseAssociatedValue
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.copyTypeParametersFrom

@MustBeExecutedAfterBridgingConfiguration
class SealedEnumGeneratorDelegate(override val context: SirPhase.Context) : SealedGeneratorExtensionContainer {

    context(SirPhase.Context)
    fun generate(kirClass: KirClass): SirClass = SirClass(
        baseName = "Sealed",
        kind = SirClass.Kind.Enum,
        parent = namespaceProvider.getNamespaceExtension(kirClass),
        isReplaced = true,
        isHidden = true,
    ).apply {
        addConformanceToHashableIfPossible(kirClass)

        copyTypeParametersFrom(kirClass.originalSirClass)

        addSealedEnumCases(kirClass)

        attributes.add("frozen")
    }

    context(SirPhase.Context)
    private fun SirClass.addConformanceToHashableIfPossible(kirClass: KirClass) {
        if (kirClass.areAllExposedChildrenHashable && kirClass.areAllSuperTypesValid) {
            this.superTypes.add(sirBuiltins.Swift.Hashable.defaultType)
        }
    }

    private fun SirClass.addSealedEnumCases(kirClass: KirClass) {
        val preferredNamesCollide = kirClass.enumCaseNamesBasedOnKotlinIdentifiersCollide

        kirClass.visibleSealedSubclasses
            .forEach { sealedSubclass ->
                addSealedEnumCase(sealedSubclass, preferredNamesCollide)
            }

        if (kirClass.hasElseCase) {
            SirEnumCase(
                simpleName = kirClass.elseCaseName,
            )
        }
    }

    private fun SirClass.addSealedEnumCase(sealedSubclass: KirClass, preferredNamesCollide: Boolean) {
        val enum = this

        SirEnumCase(
            simpleName = sealedSubclass.enumCaseName(preferredNamesCollide),
        ).apply {
            SirEnumCaseAssociatedValue(
                type = sealedSubclass.primarySirClass.getSealedSubclassType(enum),
            )
        }
    }
}

private val KirClass.areAllExposedChildrenHashable: Boolean
    get() = sealedSubclasses.all { it.primarySirClass.isHashable }

/**
 * There is a bug in the Swift compiler that causes a crash when:
 *  - a type that conforms to `Hashable` and,
 *  - some parent type uses an external Obj-C class as a type argument
 *
 *  The primary solution is to add a fake declaration of the Obj-C class (instead of just a forward declaration).
 *  However, this cannot be done for types with unknown Framework name.
 *
 *  A better solution would be to erase the problematic argument type in the Obj-C header which is done by the Kotlin compiler in some cases anyway.
 */
private val KirClass.areAllSuperTypesValid: Boolean
    get() = oirClass.areAllSuperTypesValid

private val OirClass.areAllSuperTypesValid: Boolean
    get() = superTypes.none { superType ->
        superType.typeArguments.any { it.isUnknownExternalType } || !superType.declaration.areAllSuperTypesValid
    }

private val OirType.isUnknownExternalType: Boolean
    get() = when (this) {
        is PointerOirType -> false
        is PrimitiveOirType -> false
        is BlockPointerOirType -> false
        is DeclaredOirType -> declaration.primarySirClass.module is SirModule.Unknown
        is SpecialReferenceOirType -> false
        is TypeParameterUsageOirType -> false
        is NullableReferenceOirType -> nonNullType.isUnknownExternalType
        is TypeDefOirType -> declaration.type.isUnknownExternalType
        VoidOirType -> false
    }
