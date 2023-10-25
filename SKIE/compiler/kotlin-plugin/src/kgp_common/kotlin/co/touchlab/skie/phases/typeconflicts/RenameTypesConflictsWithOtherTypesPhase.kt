package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isRemoved
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

object RenameTypesConflictsWithOtherTypesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val sortedTypeDeclarations = sirProvider.allLocalTypeDeclarations.sortedWith(getCollisionResolutionPriorityComparator())

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
    context(SirPhase.Context)
    private fun getCollisionResolutionPriorityComparator(): Comparator<SirTypeDeclaration> =
        compareBy<SirTypeDeclaration> {
            it.fqName.depth
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
            .thenByDescending { it is SirClass }
            .thenByDescending { it.getKirClassOrNull() != null }
            .thenByDescending { it.getKirClassOrNull()?.kind != KirClass.Kind.File }
            .thenBy { it.getKirClassOrNull()?.kotlinClassNestingLevel ?: 0 }
            .thenBy { it.getKirClassOrNull()?.kotlinName ?: "" }
            .thenBy { it.getOirClassOrNull()?.name?.length ?: Int.MAX_VALUE }

    private val SirFqName.depth: Int
        get() = 1 + (this.parent?.depth ?: 0)

    context(SirPhase.Context)
    private fun SirTypeDeclaration.getKirClassOrNull(): KirClass? =
        (this as? SirClass)?.let { kirProvider.findClass(it) }

    private val KirClass.kotlinName: String
        get() = when (val descriptor = this.descriptor) {
            is KirClass.Descriptor.Class -> descriptor.value.fqNameSafe.asString()
            is KirClass.Descriptor.File -> descriptor.value.name ?: ""
        }

    private val KirClass.kotlinClassNestingLevel: Int
        get() = when (val descriptor = this.descriptor) {
            is KirClass.Descriptor.Class -> descriptor.value.kotlinClassNestingLevel
            is KirClass.Descriptor.File -> 0
        }

    private val ClassDescriptor.kotlinClassNestingLevel: Int
        get() = 1 + ((this.containingDeclaration as? ClassDescriptor)?.kotlinClassNestingLevel ?: 0)

    context(SirPhase.Context)
    private fun SirTypeDeclaration.getOirClassOrNull(): OirClass? =
        (this as? SirClass)?.let { oirProvider.findClass(it) }

    context(SirPhase.Context)
    private fun buildUniqueSignatureSet(typeDeclarations: List<SirTypeDeclaration>) {
        val existingFqNames = mutableSetOf<String>()

        typeDeclarations.forEach { typeDeclaration ->
            while (typeDeclaration.fqName.toString() in existingFqNames) {
                typeDeclaration.baseName += "_"
            }

            existingFqNames.add(typeDeclaration.fqName.toString())
        }
    }
}
