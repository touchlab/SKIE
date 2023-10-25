package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirEnumCaseAssociatedValue
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.copyTypeParametersFrom

class SealedEnumGeneratorDelegate(
    override val context: SirPhase.Context,
) : SealedGeneratorExtensionContainer {

    context(SirPhase.Context)
    fun generate(kirClass: KirClass): SirClass =
        SirClass(
            baseName = "Sealed",
            kind = SirClass.Kind.Enum,
            parent = skieNamespaceProvider.getNamespace(kirClass),
            visibility = SirVisibility.PublicButReplaced,
        ).apply {
            addConformanceToHashableIfPossible(kirClass)

            copyTypeParametersFrom(kirClass.originalSirClass)

            addSealedEnumCases(kirClass)

            attributes.add("frozen")
        }

    context(SirPhase.Context)
    private fun SirClass.addConformanceToHashableIfPossible(kirClass: KirClass) {
        if (kirClass.areAllExposedChildrenHashable) {
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

    private fun SirClass.addSealedEnumCase(
        sealedSubclass: KirClass,
        preferredNamesCollide: Boolean,
    ) {
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
