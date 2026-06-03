package co.touchlab.skie.phases.oir

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase

object InitializeOirSuperTypesPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.kirProvider.allClasses.forEach {
            initializeSuperTypes(it)
        }
    }

    context(context: SirPhase.Context)
    private fun initializeSuperTypes(kirClass: KirClass) {
        val oirClass = kirClass.oirClass

        kirClass.superTypes
            .map { context.oirTypeTranslator.mapType(it) }
            .forEach { superType ->
                oirClass.superTypes.add(superType)
            }
    }
}
