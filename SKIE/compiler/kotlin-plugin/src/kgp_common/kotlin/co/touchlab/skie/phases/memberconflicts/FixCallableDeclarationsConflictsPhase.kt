package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.type.BlockPointerKirType
import co.touchlab.skie.kir.type.ErrorOutKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.kir.type.OirBasedKirType
import co.touchlab.skie.kir.type.ReferenceKirType
import co.touchlab.skie.kir.type.SuspendCompletionKirType
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isRemoved
import co.touchlab.skie.sir.element.module
import co.touchlab.skie.sir.element.receiverDeclaration
import co.touchlab.skie.sir.element.resolveAsSirClass
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerDesc
import org.jetbrains.kotlin.resolve.isValueClass
import org.jetbrains.kotlin.types.KotlinType

object FixCallableDeclarationsConflictsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val comparator = getCollisionResolutionPriorityComparator()

        val sortedCallableDeclarations = sirProvider.allLocalDeclarations
            .filterIsInstance<SirCallableDeclaration>()
            .sortedWith(comparator)

        buildUniqueSignatureSet(sortedCallableDeclarations)
    }

    /**
     * constructors without value parameters are processed first because they cannot be renamed
     * Non-removed declarations are prioritized
     * visibility (exported is prioritized)
     * originating from Kotlin stdlib (is prioritized)
     * originating from Kotlin (is prioritized)
     * true member vs extension (member is prioritized)
     * receiverDeclaration is class vs. protocol (protocol is prioritized)
     * the highest distance to root of receiverDeclaration inheritance hierarchy (lower is prioritized)
     * constructor vs property vs function (in that order)
     * number of inlined types - value classes (lower is prioritized)
     * container fqname including file
     * Kotlin signature if available
     */
    context(SirPhase.Context)
    private fun getCollisionResolutionPriorityComparator(): Comparator<SirCallableDeclaration> =
        compareByDescending<SirCallableDeclaration> {
            it is SirConstructor && it.valueParameters.isEmpty()
        }
            .thenBy { it.isRemoved }
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
            .thenByDescending { it.getKirDeclarationOrNull()?.module == context.kirBuiltins.stdlibModule }
            .thenByDescending { it.isFromKotlin }
            .thenBy { it.parent is SirExtension }
            .thenByDescending { it.receiverDeclaration?.kind == SirClass.Kind.Protocol }
            .thenBy { it.receiverDeclaration?.highestDistanceToInheritanceHierarchyRoot ?: 0 }
            .thenBy {
                when (it) {
                    is SirConstructor -> 0
                    is SirProperty -> 1
                    is SirSimpleFunction -> 2
                }
            }
            .thenBy { declaration ->
                (declaration.getKirDeclarationOrNull() as? KirFunction<*>)?.valueParameters?.count { it.type.isInlinedType } ?: 0
            }
            .thenBy { it.parent.containerFqName }
            .thenBy {
                with(KonanManglerDesc) {
                    it.getKirDeclarationOrNull()?.descriptor?.signatureString(false)
                } ?: ""
            }

    private val SirCallableDeclaration.isFromKotlin: Boolean
        get() = module is SirModule.Kotlin

    private val SirClass.highestDistanceToInheritanceHierarchyRoot: Int
        get() {
            val maxFromSuperTypes = superTypes.maxOfOrNull {
                it.resolveAsSirClass()?.highestDistanceToInheritanceHierarchyRoot ?: Int.MAX_VALUE
            }

            return 1 + (maxFromSuperTypes ?: 0)
        }

    context(SirPhase.Context)
    private fun SirCallableDeclaration.getKirDeclarationOrNull(): KirCallableDeclaration<*>? =
        kirProvider.findCallableDeclaration<SirCallableDeclaration>(this)

    private val KirType.isInlinedType: Boolean
        get() = when (this) {
            is BlockPointerKirType -> this.kotlinType.isInlinedType
            ErrorOutKirType -> false
            is OirBasedKirType -> false
            is ReferenceKirType -> this.kotlinType.isInlinedType
            is SuspendCompletionKirType -> this.kotlinType.isInlinedType
        }

    private val KotlinType.isInlinedType: Boolean
        get() = this.constructor.declarationDescriptor?.isValueClass() == true

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
