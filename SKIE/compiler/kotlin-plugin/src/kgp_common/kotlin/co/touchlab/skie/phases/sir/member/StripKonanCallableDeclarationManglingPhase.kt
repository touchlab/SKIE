package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirProperty
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.phases.SirPhase

object StripKonanCallableDeclarationManglingPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        kirProvider.allCallableDeclarations.forEach {
            it.stripMangling()
        }
    }

    private fun KirCallableDeclaration<*>.stripMangling() {
        when (this) {
            is KirSimpleFunction -> stripMangling()
            is KirConstructor -> stripMangling()
            is KirProperty -> stripMangling()
        }
    }

    private fun KirSimpleFunction.stripMangling() {
        originalSirFunction.identifier = originalSirFunction.identifier.stripMangling(name)

        valueParameters.forEach {
            it.stripMangling()
        }
    }

    private fun KirConstructor.stripMangling() {
        valueParameters.forEach {
            it.stripMangling()
        }
    }

    private fun KirValueParameter.stripMangling() {
        val sirValueParameter = originalSirValueParameter ?: return

        sirValueParameter.label = sirValueParameter.labelOrName.stripMangling(this.name)
    }

    private fun KirProperty.stripMangling() {
        originalSirProperty.identifier = originalSirProperty.identifier.stripMangling(name)
    }

    private fun String.stripMangling(kotlinName: String): String {
        val thisWithoutAnyUnderscores = this.dropLastWhile { it == '_' }
        if (thisWithoutAnyUnderscores.isBlank()) {
            return "_"
        }

        val kotlinNameUnderscores = kotlinName.takeLastWhile { it == '_' }

        return thisWithoutAnyUnderscores + kotlinNameUnderscores
    }
}
