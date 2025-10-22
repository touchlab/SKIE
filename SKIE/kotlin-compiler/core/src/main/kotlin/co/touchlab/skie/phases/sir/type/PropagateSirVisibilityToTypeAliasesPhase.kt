package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.minimumVisibility
import co.touchlab.skie.sir.type.visibilityConstraint

object PropagateSirVisibilityToTypeAliasesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allLocalTypeAliases.forEach {
            updateVisibility(it)
        }
    }

    private fun updateVisibility(sirTypeAlias: SirTypeAlias) {
        val allConstraints = listOfNotNull(
            sirTypeAlias.visibility,
            sirTypeAlias.namespace?.classDeclaration?.visibility,
        ) +
            sirTypeAlias.type.visibilityConstraint +
            sirTypeAlias.typeParameters.flatMap { typeParameter -> typeParameter.bounds.map { it.type.visibilityConstraint } }

        sirTypeAlias.visibility = allConstraints.minimumVisibility()
    }
}
