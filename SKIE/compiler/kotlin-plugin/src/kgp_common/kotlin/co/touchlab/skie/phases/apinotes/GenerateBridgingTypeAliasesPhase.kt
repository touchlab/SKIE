package co.touchlab.skie.phases.apinotes
// WIP 2 Remove commented code
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.copyTypeParametersFrom
import co.touchlab.skie.sir.element.toTypeFromEnclosingTypeParameters

object GenerateBridgingTypeAliasesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val bridgingClasses = exposedTypes.mapNotNull { it.bridgedSirClass }

        if (bridgingClasses.isEmpty()) {
            return
        }

        val bridgingTypeAliasFile = sirProvider.getFile(SirFile.skieNamespace, "BridgingTypeAliases")

        bridgingTypeAliasFile.addBridgingTypeAliases(bridgingClasses)
    }
}

private fun SirFile.addBridgingTypeAliases(bridgingClasses: List<SirClass>) {
    bridgingClasses.forEach { bridgingClass ->
        addBridgingTypeAlias(bridgingClass)
    }
}

private fun SirFile.addBridgingTypeAlias(bridgingClass: SirClass) {
    val typeAlias = SirTypeAlias(
        simpleName = "__Skie__BridgingTypeAlias__${bridgingClass.fqName.toLocalUnescapedNameString().replace(".", "_")}",
        parent = this,
        typeFactory = { bridgingClass.toTypeFromEnclosingTypeParameters(it.typeParameters) },
    )

    typeAlias.copyTypeParametersFrom(bridgingClass)

    bridgingClass.bridgingTypeAlias = typeAlias
}
