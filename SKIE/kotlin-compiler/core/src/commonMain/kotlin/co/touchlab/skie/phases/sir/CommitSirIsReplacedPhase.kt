package co.touchlab.skie.phases.sir

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirTypeDeclaration

/**
 * Prefixes all isReplaced declarations with the appropriate prefix and sets all isReplaced to false.
 *
 * This phase ensures that SKIE can use isReplaced internally without having to worry about the declarations already being replaced.
 * (For example, isReplaced is used by CreateSirMembersPhase to implement isRefinedInSwift and user-configurable SkieVisibility.)
 */
object CommitSirIsReplacedPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        sirProvider.allLocalDeclarations
            .filter { it.isReplaced }
            .forEach {
                it.commitIsReplaced()
            }
    }

    private fun SirDeclaration.commitIsReplaced() {
        when (this) {
            is SirConstructor -> error("isReplaced is not supposed to be supported for constructors: $this")
            is SirSimpleFunction -> commitIsReplaced()
            is SirProperty -> commitIsReplaced()
            is SirExtension -> error("isReplaced is not supposed to be supported for extensions: $this")
            is SirTypeDeclaration -> commitIsReplaced()
        }
    }

    private fun SirSimpleFunction.commitIsReplaced() {
        this.identifier = this.identifierAfterVisibilityChange

        this.isReplaced = false
    }

    private fun SirProperty.commitIsReplaced() {
        this.identifier = this.identifierAfterVisibilityChange

        this.isReplaced = false
    }

    private fun SirTypeDeclaration.commitIsReplaced() {
        this.baseName = this.simpleName

        this.isReplaced = false
    }
}
