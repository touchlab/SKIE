package co.touchlab.skie.phases.oir

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.toOirKind
import co.touchlab.skie.phases.SirPhase

object CreateExternalOirTypesPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.kirProvider.allExternalClasses.forEach {
            createClass(it)
        }

        context.oirProvider.initializeExternalClassCache()
    }

    context(context: SirPhase.Context)
    private fun createClass(kirClass: KirClass) {
        val oirClass = OirClass(
            name = kirClass.objCName,
            parent = context.oirProvider.externalModule,
            kind = kirClass.kind.toOirKind(),
            origin = OirClass.Origin.Kir(kirClass),
        )

        kirClass.oirClass = oirClass

        CreateKotlinOirTypesPhase.createTypeParameters(kirClass, oirClass)
    }
}
