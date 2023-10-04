package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirEnumCaseAssociatedValue
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel

class SealedEnumGeneratorDelegate(
    override val context: SirPhase.Context,
) : SealedGeneratorExtensionContainer {

    context(SirPhase.Context)
    fun generate(swiftModel: KotlinClassSwiftModel): SirClass =
        SirClass(
            baseName = "Sealed",
            kind = SirClass.Kind.Enum,
            parent = sirProvider.getSkieNamespace(swiftModel),
            visibility = SirVisibility.PublicButReplaced,
        ).apply {
            addConformanceToHashableIfPossible(swiftModel)

            copyTypeParametersFrom(swiftModel.primarySirClass)

            addSealedEnumCases(swiftModel)

            attributes.add("frozen")
        }

    context(SirPhase.Context)
    private fun SirClass.addConformanceToHashableIfPossible(swiftModel: KotlinClassSwiftModel) {
        if (swiftModel.areAllExposedChildrenHashable) {
            this.superTypes.add(sirBuiltins.Swift.Hashable.defaultType)
        }
    }

    context(SwiftModelScope)
    private fun SirClass.addSealedEnumCases(swiftModel: KotlinClassSwiftModel) {
        val preferredNamesCollide = swiftModel.enumCaseNamesBasedOnKotlinIdentifiersCollide

        swiftModel.visibleSealedSubclasses
            .forEach { sealedSubclass ->
                addSealedEnumCase(sealedSubclass, preferredNamesCollide)
            }

        if (swiftModel.hasElseCase) {
            SirEnumCase(
                simpleName = swiftModel.elseCaseName,
            )
        }
    }

    context(SwiftModelScope)
    private fun SirClass.addSealedEnumCase(
        sealedSubclass: KotlinClassSwiftModel,
        preferredNamesCollide: Boolean,
    ) {
        val enum = this

        SirEnumCase(
            simpleName = sealedSubclass.enumCaseName(preferredNamesCollide),
        ).apply {
            SirEnumCaseAssociatedValue(
                type = sealedSubclass.primarySirClass.getSealedSubclassType(enum, this@SwiftModelScope),
            )
        }
    }
}

private val KotlinClassSwiftModel.areAllExposedChildrenHashable: Boolean
    get() = exposedSealedSubclasses.all { it.primarySirClass.isHashable }
