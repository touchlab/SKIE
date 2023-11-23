package co.touchlab.skie.phases.other

import co.touchlab.skie.kir.element.DeprecationLevel
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirElementWithFunctionBodyBuilder
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.util.swift.quoteAsSwiftLiteral

object AddAvailabilityBasedDeprecationLevelPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        sirProvider.allSkieGeneratedCallableDeclarations.forEach {
            it.applyDeprecationLevel()
        }
    }

    private fun SirCallableDeclaration.applyDeprecationLevel() {
        when (val deprecationLevel = this.deprecationLevel) {
            is DeprecationLevel.Error -> {
                this.addAvailableAttribute("unavailable", deprecationLevel.message)

                this.replaceBodyWithError()
            }
            is DeprecationLevel.Warning -> this.addAvailableAttribute("deprecated", deprecationLevel.message)
            DeprecationLevel.None -> {}
        }
    }

    private fun SirCallableDeclaration.addAvailableAttribute(type: String, message: String?) {
        val messageParameter = if (message != null) ", message: ${message.quoteAsSwiftLiteral()}" else ""

        this.attributes.add("available(*, $type$messageParameter)")
    }

    private fun SirCallableDeclaration.replaceBodyWithError() {
        when (this) {
            is SirFunction -> (this as SirElementWithFunctionBodyBuilder).replaceBodyWithError()
            is SirProperty -> {
                this.getter?.replaceBodyWithError()
                this.setter?.replaceBodyWithError()
            }
        }
    }

    private fun SirElementWithFunctionBodyBuilder.replaceBodyWithError() {
        this.bodyBuilder.clear()
        this.bodyBuilder.add {
            addStatement("fatalError(\"Unavailable\")")
        }
    }
}
