package co.touchlab.skie.phases.sir

import co.touchlab.skie.oir.element.OirConstructor
import co.touchlab.skie.oir.element.OirProperty
import co.touchlab.skie.oir.element.OirSimpleFunction
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.resolveCollisionWithWarning

object RenameNonAsciiDeclarationPhase: SirPhase {
    private val firstCharAllowedChars: Set<Char> = (('a'..'z') + ('A' .. 'Z') + '_').toSet()
    private val nextCharAllowedChars: Set<Char> = firstCharAllowedChars + ('0' .. '9')

    context(SirPhase.Context)
    override suspend fun execute() {
        oirProvider.kotlinClassesAndProtocols
            .filterNot { isValidAsciiIdentifier(it.originalSirClass.baseName) }
            .forEach {
                it.originalSirClass.baseName = it.name
            }
    }

    private fun isValidAsciiIdentifier(identifier: String): Boolean {
        if (identifier.isEmpty()) {
            return false
        }

        identifier.forEachIndexed { index, c ->
            if (index == 0) {
                if (!firstCharAllowedChars.contains(c)) {
                    return false
                }
            } else {
                if (!nextCharAllowedChars.contains(c)) {
                    return false
                }
            }
        }

        return true
    }
}
