package co.touchlab.skie.phases.typeconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.module
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

object RenameTypesConflictsWithOtherTypesPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val sortedTypeDeclarations = sirProvider.allLocalTypes.sortedByCollisionResolutionPriority()

        buildUniqueSignatureSet(sortedTypeDeclarations)
    }

    context(SwiftModelScope)
    private fun List<SirTypeDeclaration>.sortedByCollisionResolutionPriority(): List<SirTypeDeclaration> =
        this.map { it to it.getCollisionResolutionPriority() }.sortedByDescending { it.second }.map { it.first }

    context(SwiftModelScope)
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

/**
 * immutable modules are prioritized because their declarations cannot be renamed
 * nested classes are renamed first so that their fqName is not changed before their renaming is resolved
 * classes are prioritized over type aliases
 * was original identifier changed (unchanged is prioritized)
 * Kotlin SirClasses are prioritized
 * Kotlin SirClasses with shorter Obj-C names are prioritized
 * Kotlin class vs file (class is prioritized)
 * visibility (Visible/public, Hidden, Replaced, Removed/non-public)
 * hash of Kotlin fqName if available
 */
// WIP 2 Logic needs to be updated
context(SwiftModelScope)
private fun SirTypeDeclaration.getCollisionResolutionPriority(): Long {
    val swiftModel = (this as? SirClass)?.swiftModelOrNull

    var priority = 0L
    if (!module.isMutable) {
        priority += 1
    }

    priority = priority shl 1
    if (namespace != null) {
        priority += 1
    }

    priority = priority shl 1
    if (this is SirClass) {
        priority += 1
    }

    priority = priority shl 1
    if (fqName == originalFqName) {
        priority += 1
    }

    priority = priority shl 1
    if (swiftModel != null) {
        priority += 1
    }

    priority = priority shl 8
    if (swiftModel != null) {
        priority += 255 - swiftModel.objCFqName.name.length.coerceAtMost(255)
    }

    priority = priority shl 1
    if (swiftModel?.kind != KotlinTypeSwiftModel.Kind.File) {
        priority += 1
    }

    priority = priority shl 2
    priority += when (visibility) {
        SirVisibility.Public -> 3
        SirVisibility.PublicButHidden -> 2
        SirVisibility.PublicButReplaced -> 1
        else -> 0
    }
    if (swiftModel == null && this.visibility == SirVisibility.Public) {
        priority += 3
    }

    priority = priority shl 32
    priority += when (val descriptorHolder = swiftModel?.descriptorHolder) {
        is ClassOrFileDescriptorHolder.Class -> descriptorHolder.value.fqNameSafe.asString().hashCode()
        is ClassOrFileDescriptorHolder.File -> descriptorHolder.value.name?.hashCode() ?: 0
        null -> 0
    }

    return priority
}
