package co.touchlab.skie.phases.oir

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.phases.SirPhase

object InitializeOirSuperTypesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.allClasses.forEach {
            initializeSuperTypes(it)
        }
    }

    context(SirPhase.Context)
    private fun initializeSuperTypes(kirClass: KirClass) {
        val oirClass = kirClass.oirClass

        kirClass.superTypes
            .map { oirTypeTranslator.mapType(it) }
            .forEach { superType ->
                oirClass.superTypes.add(superType)
            }
    }
}
