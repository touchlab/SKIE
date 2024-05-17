package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirDeclarationWithVisibility
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.getAllDeclarationsRecursively

/**
 * Changes `SirVisibility.Private` to `SirVisibility.Removed` in all Kotlin declarations.
 */
object NormalizeKotlinSirPrivateVisibilityPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.kotlinModule.getAllDeclarationsRecursively()
            .filterIsInstance<SirDeclarationWithVisibility>()
            .forEach {
                normalizeVisibility(it)
            }
    }

    private fun normalizeVisibility(sirDeclarationWithVisibility: SirDeclarationWithVisibility) {
        if (sirDeclarationWithVisibility.visibility == SirVisibility.Private) {
            sirDeclarationWithVisibility.visibility = SirVisibility.Removed
        }
    }
}
