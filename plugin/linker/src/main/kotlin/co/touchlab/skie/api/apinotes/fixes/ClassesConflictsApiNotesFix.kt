package co.touchlab.skie.api.apinotes.fixes

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import co.touchlab.skie.plugin.api.module.SkieModule
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

// TODO Currently does not take into account bridging and generated Swift code in general
class ClassesConflictsApiNotesFix(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) {

    fun fixNames() {
        skieModule.configure(SkieModule.Ordering.Last) {
            val allModels = descriptorProvider.exposedClasses.map { it.swiftModel } +
                descriptorProvider.exposedFiles.map { it.swiftModel }

            val sortedModels = allModels.sortedByCollisionResolutionPriority()

            buildUniqueSignatureSet(sortedModels)
        }
    }

    context(SwiftModelScope)
    private fun List<MutableKotlinTypeSwiftModel>.sortedByCollisionResolutionPriority(): List<MutableKotlinTypeSwiftModel> =
        this.sortedByDescending { it.collisionResolutionPriority }

    /**
     * nested classes are renamed first so that their fqName is not changed before their renaming is resolved
     * was original identifier changed (unchanged is prioritized)
     * class vs file (class is prioritized)
     * visibility (Visible, Hidden, Replaced, Removed)
     * hash of Kotlin fqName
     */
    private val MutableKotlinTypeSwiftModel.collisionResolutionPriority: Long
        get() {
            var priority = 0L

            if (this.containingType != null) {
                priority += 1
            }

            priority = priority shl 1
            if (this.identifier == this.original.identifier) {
                priority += 1
            }

            priority = priority shl 1
            if (this is KotlinClassSwiftModel) {
                priority += 1
            }

            priority = priority shl 2
            priority += when (this.visibility) {
                SwiftModelVisibility.Visible -> 3
                SwiftModelVisibility.Hidden -> 2
                SwiftModelVisibility.Replaced -> 1
                SwiftModelVisibility.Removed -> 0
            }

            priority = priority shl 32
            priority += when (val descriptorHolder = this.descriptorHolder) {
                is ClassOrFileDescriptorHolder.Class -> descriptorHolder.value.fqNameSafe.asString().hashCode()
                is ClassOrFileDescriptorHolder.File -> descriptorHolder.value.name?.hashCode() ?: 0
            }

            return priority
        }

    private fun buildUniqueSignatureSet(models: List<MutableKotlinTypeSwiftModel>) {
        val existingFqNames = mutableSetOf<String>()

        models.forEach { model ->
            while (model.fqName in existingFqNames) {
                model.identifier += "_"
            }

            existingFqNames.add(model.fqName)
        }
    }
}

