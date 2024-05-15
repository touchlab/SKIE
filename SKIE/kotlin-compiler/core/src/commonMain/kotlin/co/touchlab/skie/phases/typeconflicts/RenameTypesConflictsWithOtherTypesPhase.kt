package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isRemoved
import co.touchlab.skie.sir.element.oirClassOrNull
import co.touchlab.skie.sir.element.resolveAsKirClass
import co.touchlab.skie.util.resolveCollisionWithWarning

object RenameTypesConflictsWithOtherTypesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val sortedTypeDeclarations = sirProvider.allLocalTypeDeclarations.sortedWith(collisionResolutionPriorityComparator)

        buildUniqueSignatureSet(sortedTypeDeclarations)
    }

    /**
     * fqName depth / number of parents (top-level is prioritized which is needed due to the way the algorithm caches used signatures)
     * Non-removed declarations are prioritized
     * visibility (exported is prioritized)
     * classes are prioritized over type aliases
     * originating from Kotlin (is prioritized)
     * Kotlin class vs file (class is prioritized)
     * Kotlin class nesting level if available (lower is prioritized)
     * Kotlin fqName if available
     * Kotlin SirClasses with shorter Obj-C names are prioritized
     */
    private val collisionResolutionPriorityComparator: Comparator<SirTypeDeclaration>
        get() = compareBy<SirTypeDeclaration> {
            it.fqName.depth
        }
            .thenBy { it.isRemoved }
            .thenBy {
                when (it.visibility) {
                    SirVisibility.Public -> 0
                    SirVisibility.PublicButHidden -> 1
                    SirVisibility.Internal -> 2
                    SirVisibility.Private -> 3
                    SirVisibility.Removed -> 4
                }
            }
            .thenBy { it.isReplaced }
            .thenByDescending { it is SirClass }
            .thenByDescending { it.resolveAsKirClass() != null }
            .thenByDescending { it.resolveAsKirClass()?.kind != KirClass.Kind.File }
            .thenBy { it.resolveAsKirClass()?.nestingLevel ?: 0 }
            .thenBy { it.resolveAsKirClass()?.kotlinFqName ?: "" }
            .thenBy { it.getOirClassOrNull()?.name?.length ?: Int.MAX_VALUE }

    private val SirFqName.depth: Int
        get() = 1 + (this.parent?.depth ?: 0)

    private fun SirTypeDeclaration.getOirClassOrNull(): OirClass? =
        (this as? SirClass)?.oirClassOrNull

    context(SirPhase.Context)
    private fun buildUniqueSignatureSet(typeDeclarations: List<SirTypeDeclaration>) {
        val existingFqNames = mutableSetOf<String>()

        typeDeclarations.forEach { typeDeclaration ->
            typeDeclaration.resolveCollisionWithWarning {
                if (typeDeclaration.fqName.toString() in existingFqNames) {
                    "an another type named '${typeDeclaration.fqName}'"
                } else {
                    null
                }
            }

            existingFqNames.add(typeDeclaration.fqName.toString())
        }
    }
}
