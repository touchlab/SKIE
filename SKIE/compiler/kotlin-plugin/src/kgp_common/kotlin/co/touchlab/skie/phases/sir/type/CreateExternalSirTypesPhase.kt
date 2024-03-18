package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.toSirKind

object CreateExternalSirTypesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        oirProvider.allExternalClassesAndProtocols.forEach {
            createClass(it)
        }
    }

    context(SirPhase.Context)
    private fun createClass(oirClass: OirClass): SirClass {
        val sirClass = SirClass(
            baseName = oirClass.name,
            parent = sirProvider.findExternalModule(oirClass)?.builtInFile ?: SirModule.Unknown.builtInFile,
            kind = oirClass.kind.toSirKind(),
            origin = SirClass.Origin.Oir(oirClass),
        )

        oirClass.originalSirClass = sirClass

        return sirClass
    }
}
