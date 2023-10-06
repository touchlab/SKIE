package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirValueParameterParent
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.module
import co.touchlab.skie.sir.type.TypeParameterUsageSirType

// WIP 2 needs another phase that solves conflicts between global callable declarations and types
class FixCallableDeclarationsConflictsPhase(
    private val context: SirPhase.Context,
) : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val sortedMembers = sirProvider.allLocalDeclarations
            .filterIsInstance<SirCallableDeclaration>()
            .sortedWith(collisionResolutionPriorityComparator)

        buildUniqueSignatureSet(sortedMembers)
    }

    /**
     * constructors without value parameters are processed first because they cannot be renamed
     * visibility (exported is prioritized)
     * built in (is prioritized)
     * originating from Kotlin (is prioritized)
     * base vs inherited (base is prioritized)
     * true member vs extension (member is prioritized)
     * constructor vs property vs function (in that order)
     * number of substituted types in parameter types (lower is better)
     * container fqname including file
     */
    private val collisionResolutionPriorityComparator =
        compareByDescending<SirCallableDeclaration> {
            it is SirConstructor && it.valueParameters.isEmpty()
        }
            .thenBy {
                when (it.visibility) {
                    SirVisibility.Public -> 0
                    SirVisibility.PublicButHidden -> 1
                    SirVisibility.PublicButReplaced -> 2
                    SirVisibility.Internal -> 3
                    SirVisibility.Private -> 4
                    SirVisibility.Removed -> 5
                }
            }
            .thenByDescending { it.isBuiltin }
            .thenByDescending { it.isFromKotlin }
            .thenBy { (it as? SirFunction)?.overriddenDeclarations?.size ?: 0 }
            .thenBy { it.parent is SirExtension }
            .thenBy {
                when (it) {
                    is SirConstructor -> 0
                    is SirProperty -> 1
                    is SirFunction -> 2
                }
            }
            .thenBy {
                (it as? SirValueParameterParent)?.valueParameters?.count { parameter -> parameter.type is TypeParameterUsageSirType } ?: 0
            }
            .thenBy { it.parent.containerFqName }

    private val SirDeclaration.isBuiltin: Boolean
        get() = this in context.sirBuiltins.Kotlin.allBuiltInsWithDescriptors

    private val SirDeclaration.isFromKotlin: Boolean
        get() = module is SirModule.Kotlin

    @Suppress("RecursivePropertyAccessor")
    private val SirDeclarationParent.containerFqName: String
        get() = (this.parent?.containerFqName ?: "") + this.toString()

    private fun buildUniqueSignatureSet(callableDeclarations: List<SirCallableDeclaration>) {
        val signatureSet = UniqueSignatureSet()

        callableDeclarations.forEach {
            signatureSet.add(it)
        }
    }
}
