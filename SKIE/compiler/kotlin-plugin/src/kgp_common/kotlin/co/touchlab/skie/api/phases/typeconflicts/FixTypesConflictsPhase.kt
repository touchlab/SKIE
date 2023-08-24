package co.touchlab.skie.api.phases.typeconflicts

import co.touchlab.skie.api.phases.SkieLinkingPhase
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.ObjcSwiftBridge
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.sir.SwiftFqName
import co.touchlab.skie.plugin.api.sir.declaration.BuiltinDeclarations
import co.touchlab.skie.plugin.api.sir.declaration.SwiftIrTypeDeclaration
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

// TODO Currently does not take into account bridging and generated Swift code in general
class FixTypesConflictsPhase(
    private val skieModule: SkieModule,
    private val builtinKotlinDeclarations: BuiltinDeclarations.Kotlin,
) : SkieLinkingPhase {

    private val reservedNames by lazy {
        builtinKotlinDeclarations.allDeclarations.map { it.publicName } +
            // TODO: Unfortunate hack to avoid name collision with Swift's Any keyword
            SwiftFqName.Local.TopLevel("Any")
    }

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.Last) {
            val sortedModels = exposedTypes.sortedByCollisionResolutionPriority()

            buildUniqueSignatureSet(sortedModels)
        }
    }

    context(SwiftModelScope)
    private fun List<MutableKotlinTypeSwiftModel>.sortedByCollisionResolutionPriority(): List<MutableKotlinTypeSwiftModel> =
        this.map { it to it.collisionResolutionPriority }.sortedByDescending { it.second }.map { it.first }

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
            if (this.identifier == this.originalIdentifier) {
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
        val existingFqNames = reservedNames.toMutableSet()

        models.forEach { model ->
            while (model.nonBridgedDeclaration.publicName in existingFqNames) {
                model.identifier += "_"
            }
            existingFqNames.add(model.nonBridgedDeclaration.publicName)

            val existingBridge = model.bridge as? ObjcSwiftBridge.FromSKIE ?: return@forEach
            val localBridgeDeclaration = existingBridge.declaration as? SwiftIrTypeDeclaration.Local.SKIEGeneratedSwiftType ?: return@forEach
            while (localBridgeDeclaration.publicName in existingFqNames) {
                localBridgeDeclaration.swiftName += "_"
            }
            existingFqNames.add(localBridgeDeclaration.publicName)
        }
    }
}
