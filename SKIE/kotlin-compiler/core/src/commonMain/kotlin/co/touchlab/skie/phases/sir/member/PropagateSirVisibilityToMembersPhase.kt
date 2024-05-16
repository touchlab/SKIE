package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.minimumVisibility
import co.touchlab.skie.sir.type.visibilityConstraint

object PropagateSirVisibilityToMembersPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allLocalDeclarations
            .filterIsInstance<SirCallableDeclaration>()
            .forEach {
                updateVisibility(it)
            }
    }

    private fun updateVisibility(sirCallableDeclaration: SirCallableDeclaration) {
        when (sirCallableDeclaration) {
            is SirConstructor -> updateVisibility(sirCallableDeclaration)
            is SirSimpleFunction -> updateVisibility(sirCallableDeclaration)
            is SirProperty -> updateVisibility(sirCallableDeclaration)
        }
    }

    private fun updateVisibility(sirConstructor: SirConstructor) {
        val allConstraints = listOfNotNull(
            sirConstructor.visibility,
            sirConstructor.parent.classDeclaration.visibility,
        ) +
            sirConstructor.valueParameters.map { it.type.visibilityConstraint }

        sirConstructor.visibility = allConstraints.minimumVisibility()
    }

    private fun updateVisibility(sirFunction: SirSimpleFunction) {
        val allConstraints = listOfNotNull(
            sirFunction.visibility,
            sirFunction.memberOwner?.visibility,
            sirFunction.returnType.visibilityConstraint,
        ) +
            sirFunction.valueParameters.map { it.type.visibilityConstraint } +
            sirFunction.typeParameters.flatMap { typeParameter -> typeParameter.bounds.map { it.visibilityConstraint } }

        sirFunction.visibility = allConstraints.minimumVisibility()
    }

    private fun updateVisibility(sirProperty: SirProperty) {
        val allConstraints = listOfNotNull(
            sirProperty.visibility,
            sirProperty.memberOwner?.visibility,
            sirProperty.type.visibilityConstraint,
        )

        sirProperty.visibility = allConstraints.minimumVisibility()
    }
}
