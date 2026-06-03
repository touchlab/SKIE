package co.touchlab.skie.phases.sir.type

import co.touchlab.skie.oir.element.OirExtension
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.topLevelParent

object CreateKotlinSirExtensionsPhase : SirPhase {

    context(context: SirPhase.Context)
    override suspend fun execute() {
        context.oirProvider.kotlinExtensions.forEach {
            createExtension(it)
        }
    }

    context(context: SirPhase.Context)
    private fun createExtension(oirExtension: OirExtension) {
        val sirClass = oirExtension.classDeclaration.originalSirClass

        oirExtension.sirExtension = context.sirProvider.getExtension(sirClass, sirClass.topLevelParent)
    }
}
