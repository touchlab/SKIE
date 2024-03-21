package co.touchlab.skie.phases.oir

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirConstructor
import co.touchlab.skie.oir.element.constructors
import co.touchlab.skie.oir.element.copyValueParametersFrom
import co.touchlab.skie.oir.element.superClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.shallowCopy

object CreateFakeObjCConstructorsPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        oirProvider.allKotlinClasses.forEach {
            it.addFakeObjCConstructors()
        }
    }

    private fun OirClass.addFakeObjCConstructors() {
        val superClass = superClass ?: return

        val constructorObjCSelectors = this.constructors.map { it.selector }.toSet()

        val missingConstructors = superClass.constructors.filter { it.selector !in constructorObjCSelectors }

        missingConstructors.forEach {
            addFakeObjCConstructor(it)
        }
    }

    private fun OirClass.addFakeObjCConstructor(parentConstructor: OirConstructor) {
        val oirConstructor = OirConstructor(
            selector = parentConstructor.selector,
            parent = this,
            errorHandlingStrategy = parentConstructor.errorHandlingStrategy,
            deprecationLevel = parentConstructor.deprecationLevel,
        ).apply {
            copyValueParametersFrom(parentConstructor)
        }

        oirConstructor.originalSirConstructor = parentConstructor.originalSirConstructor.shallowCopy(
            parent = this.originalSirClass,
            visibility = when (parentConstructor.originalSirConstructor.visibility) {
                SirVisibility.Removed -> SirVisibility.Removed
                else -> SirVisibility.Private
            },
        ).apply {
            copyValueParametersFrom(parentConstructor.originalSirConstructor)
        }
    }
}
