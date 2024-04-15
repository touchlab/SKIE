package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.kirClassOrNull
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.toSirKind

object CreateExternalSirTypesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        oirProvider.externalClassesAndProtocols.forEach {
            createClass(it)
        }
    }

    context(SirPhase.Context)
    private fun createClass(oirClass: OirClass) {
        val sirClass = SirClass(
            baseName = oirClass.name,
            parent = oirClass.kirClassOrNull?.let { sirProvider.findExternalModule(it) }?.builtInFile ?: sirProvider.unknownModule.builtInFile,
            kind = oirClass.kind.toSirKind(),
            origin = SirClass.Origin.Oir(oirClass),
        )

        oirClass.originalSirClass = sirClass

        CreateKotlinSirTypesPhase.createTypeParameters(oirClass, sirClass)
    }
}
