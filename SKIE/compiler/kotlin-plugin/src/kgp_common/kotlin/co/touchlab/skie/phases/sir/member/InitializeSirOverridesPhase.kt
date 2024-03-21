package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.kir.element.KirOverridableDeclaration
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.phases.SirPhase

object InitializeSirOverridesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.allOverridableDeclaration.forEach(::initializeOverrides)
    }

    private fun initializeOverrides(overridableDeclaration: KirOverridableDeclaration<*, *>) {
        when (overridableDeclaration) {
            is KirSimpleFunction -> initializeOverrides(overridableDeclaration)
            is KirProperty -> initializeOverrides(overridableDeclaration)
        }
    }

    private fun initializeOverrides(kirSimpleFunction: KirSimpleFunction) {
        kirSimpleFunction.overriddenDeclarations.forEach { overriddenDeclaration ->
            kirSimpleFunction.originalSirFunction.addOverride(overriddenDeclaration.originalSirFunction)

            val overriddenBridge = kirSimpleFunction.bridgedSirFunction
            val overrideBridge = overriddenDeclaration.bridgedSirFunction

            if (overriddenBridge != null && overrideBridge != null) {
                overriddenBridge.addOverride(overrideBridge)
            }
        }
    }

    private fun initializeOverrides(kirProperty: KirProperty) {
        kirProperty.overriddenDeclarations.forEach { overriddenDeclaration ->
            kirProperty.originalSirProperty.addOverride(overriddenDeclaration.originalSirProperty)
        }
    }
}
