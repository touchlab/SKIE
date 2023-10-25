package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirModule
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.toSirKind

object CreateExternalSirTypesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        oirProvider.allExternalModules.forEach { module ->
            module.declarations.filterIsInstance<OirClass>().forEach {
                createClass(it, module)
            }
        }
    }

    context(SirPhase.Context)
    private fun createClass(oirClass: OirClass, module: OirModule.External): SirClass {
        val sirClass = SirClass(
            baseName = oirClass.name,
            parent = sirProvider.getExternalModule(module.name),
            kind = oirClass.kind.toSirKind(),
        )

        oirClass.originalSirClass = sirClass

        return sirClass
    }
}
