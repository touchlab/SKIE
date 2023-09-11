package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.sir.element.SirClass
import co.touchlab.skie.plugin.api.sir.element.SirTypeDeclaration
import co.touchlab.skie.plugin.api.sir.element.SirVisibility
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class RenameTypesConflictsWithOtherTypesPhase(
    private val skieModule: SkieModule,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.First) {
            val sortedTypeDeclarations = sirProvider.allLocalTypes.sortedByCollisionResolutionPriority()

            buildUniqueSignatureSet(sortedTypeDeclarations)
        }
    }

    context(SwiftModelScope)
    private fun List<SirTypeDeclaration>.sortedByCollisionResolutionPriority(): List<SirTypeDeclaration> =
        this.map { it to it.getCollisionResolutionPriority() }.sortedByDescending { it.second }.map { it.first }

    context(SwiftModelScope)
    private fun buildUniqueSignatureSet(typeDeclarations: List<SirTypeDeclaration>) {
        val existingFqNames = mutableSetOf<String>()

        typeDeclarations.forEach { typeDeclaration ->
            while (typeDeclaration.fqName.toString() in existingFqNames) {
                typeDeclaration.simpleName += "_"
            }

            existingFqNames.add(typeDeclaration.fqName.toString())
        }
    }
}

/**
 * nested classes are renamed first so that their fqName is not changed before their renaming is resolved
 * was original identifier changed (unchanged is prioritized)
 * Kotlin SirClasses are prioritized
 * Kotlin SirClasses with shorter Obj-C names are prioritized
 * Kotlin class vs file (class is prioritized)
 * visibility (Visible/public, Hidden, Replaced, Removed/non-public)
 * hash of Kotlin fqName if available
 */
// WIP typealiases are deprioritized
context(SwiftModelScope)
private fun SirTypeDeclaration.getCollisionResolutionPriority(): Long {
    val swiftModel = (this as? SirClass)?.swiftModelOrNull

    var priority = 0L
    if (namespace != null) {
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
    priority += when (swiftModel?.visibility) {
        SwiftModelVisibility.Visible -> 3
        SwiftModelVisibility.Hidden -> 2
        SwiftModelVisibility.Replaced -> 1
        SwiftModelVisibility.Removed, null -> 0
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
