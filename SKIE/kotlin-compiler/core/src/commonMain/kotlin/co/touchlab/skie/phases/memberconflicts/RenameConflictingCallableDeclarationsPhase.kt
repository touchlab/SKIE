package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isRemoved
import co.touchlab.skie.sir.element.module
import co.touchlab.skie.sir.element.receiverDeclaration
import co.touchlab.skie.sir.element.resolveAsSirClass

class RenameConflictingCallableDeclarationsPhase : SirPhase {

    private val highestDistanceToInheritanceHierarchyRootCache = mutableMapOf<SirClass, Int>()
    private val containerFqNameCache = mutableMapOf<SirDeclarationParent, String>()

    context(SirPhase.Context)
    override suspend fun execute() {
        val sortedEnumCases = getSortedEnumCases()
        val sortedCallableDeclarations = getSortedCallableDeclarations()

        val uniqueSignatureSet = UniqueSignatureSet()

        uniqueSignatureSet.addEnumCases(sortedEnumCases)
        uniqueSignatureSet.addCallableDeclarations(sortedCallableDeclarations)
    }

    context(SirPhase.Context)
    private fun getSortedEnumCases(): List<SirEnumCase> = sirProvider.allLocalEnums.flatMap { it.enumCases }

    context(SirPhase.Context)
    private fun getSortedCallableDeclarations(): List<SirCallableDeclaration> {
        val comparator = getCollisionResolutionPriorityComparator()

        return sirProvider.allLocalCallableDeclarations.sortedWith(comparator)
    }

    /**
     * constructors without value parameters are processed first because they cannot be renamed
     * Kotlin enum entries are prioritized and sorted by index
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
            .thenBy { declaration ->
                (declaration as? SirProperty)?.let { kirProvider.findEnumEntry(it)?.index } ?: Int.MAX_VALUE
            }
            .thenBy { it.isRemoved }
            .thenBy {
                when (it.visibility) {
                    SirVisibility.Public -> 0
                    SirVisibility.Internal -> 1
                    SirVisibility.Private -> 2
                    SirVisibility.Removed -> 3
                }
            }
            .thenBy { it.visibility == SirVisibility.Public && it.isHidden }
            .thenBy { it.isReplaced }
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
                (declaration.getKirDeclarationOrNull() as? KirFunction<*>)?.valueParameters?.count { it.wasTypeInlined } ?: 0
            }
            .thenBy { it.parent.containerFqName }
            .thenBy { it.getKirDeclarationOrNull()?.kotlinSignature ?: "" }

    private val SirCallableDeclaration.isFromKotlin: Boolean
        get() = module is SirModule.Kotlin

    private val SirClass.highestDistanceToInheritanceHierarchyRoot: Int
        get() = highestDistanceToInheritanceHierarchyRootCache.getOrPut(this) {
            val maxFromSuperTypes = superTypes.maxOfOrNull {
                it.resolveAsSirClass()?.highestDistanceToInheritanceHierarchyRoot ?: Int.MAX_VALUE
            }

            return 1 + (maxFromSuperTypes ?: 0)
        }

    context(SirPhase.Context)
    private fun SirCallableDeclaration.getKirDeclarationOrNull(): KirCallableDeclaration<*>? =
        kirProvider.findCallableDeclaration<SirCallableDeclaration>(this)

    @Suppress("RecursivePropertyAccessor")
    private val SirDeclarationParent.containerFqName: String
        get() = containerFqNameCache.getOrPut(this) {
            (this.parent?.containerFqName ?: "") + this.toString()
        }

    context(SirPhase.Context)
    private fun UniqueSignatureSet.addEnumCases(enumCases: List<SirEnumCase>) {
        enumCases.forEach {
            this.add(it)
        }
    }

    context(SirPhase.Context)
    private fun UniqueSignatureSet.addCallableDeclarations(callableDeclarations: List<SirCallableDeclaration>) {
        callableDeclarations.forEach {
            this.add(it)
        }
    }
}
