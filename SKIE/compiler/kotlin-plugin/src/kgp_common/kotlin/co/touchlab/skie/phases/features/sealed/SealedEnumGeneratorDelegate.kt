package co.touchlab.skie.phases.features.sealed

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.KotlinClassSwiftModel
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirEnumCaseAssociatedValue
import co.touchlab.skie.sir.element.copyTypeParametersFrom

class SealedEnumGeneratorDelegate(
    override val context: SirPhase.Context,
) : SealedGeneratorExtensionContainer {

    context(SwiftModelScope)
    fun generate(swiftModel: KotlinClassSwiftModel): SirClass {
        val enum = SirClass(
            simpleName = "__Sealed",
            kind = SirClass.Kind.Enum,
            parent = sirProvider.getSkieNamespace(swiftModel),
        )

        enum.copyTypeParametersFrom(swiftModel.primarySirClass)

        enum.addSealedEnumCases(swiftModel)

        enum.swiftPoetBuilderModifications.add {
            addAttribute("frozen")
        }

        return enum
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
                parent = this,
            )
        }
    }

    context(SwiftModelScope)
    private fun SirClass.addSealedEnumCase(
        sealedSubclass: KotlinClassSwiftModel,
        preferredNamesCollide: Boolean,
    ) {
        val enum = this

        val enumCase = SirEnumCase(
            simpleName = sealedSubclass.enumCaseName(preferredNamesCollide),
            parent = this,
        )

        SirEnumCaseAssociatedValue(
            type = sealedSubclass.primarySirClass.getSealedSubclassType(enum, this@SwiftModelScope),
            parent = enumCase,
        )
    }
}
