package co.touchlab.skie.api.apinotes.fixes

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.collisionFreeIdentifier
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import co.touchlab.skie.plugin.api.module.SkieModule

class ClassInsideNonExportedClassApiNotesFix(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) {

    fun renameProblematicClasses() {
        skieModule.configure(SkieModule.Ordering.Last) {
            val existingNames = descriptorProvider.classDescriptors.map { it.swiftModel.fqName }.toMutableSet()

            descriptorProvider.classDescriptors
                .map { it.swiftModel }
                .filter { it.needsRenaming }
                .forEach {
                    it.removeContainingType(existingNames)
                }
        }
    }

    private fun MutableKotlinClassSwiftModel.removeContainingType(existingNames: MutableSet<String>) {
        val mergedIdentifier = this.containingType!!.identifier + this.identifier

        this.identifier = mergedIdentifier.collisionFreeIdentifier(existingNames)
        existingNames.add(this.identifier)

        this.containingType = null
    }

    private val KotlinClassSwiftModel.needsRenaming: Boolean
        get() {
            val containingDescriptor = this.containingType?.classDescriptor ?: return false

            return !descriptorProvider.shouldBeExposed(containingDescriptor)
        }
}
