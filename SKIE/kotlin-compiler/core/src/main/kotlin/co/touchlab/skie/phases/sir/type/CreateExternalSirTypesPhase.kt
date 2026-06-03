package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.toSirKind

object CreateExternalSirTypesPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.oirProvider.externalClassesAndProtocols.forEach {
            createClass(it)
        }
    }

    context(context: SirPhase.Context)
    private fun createClass(oirClass: OirClass) {
        val sirClass = SirClass(
            baseName = oirClass.name,
            parent = context.sirProvider.getModuleForExternalClass(oirClass).builtInFile,
            kind = oirClass.kind.toSirKind(),
            origin = SirClass.Origin.Oir(oirClass),
        )

        oirClass.originalSirClass = sirClass

        CreateKotlinSirTypesPhase.createTypeParameters(oirClass, sirClass)
    }
}
