package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SkieLinkingPhase
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.kir.allExposedMembers
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.phases.SkieModule
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isOverridable
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.overriddenTreeAsSequence
import org.jetbrains.kotlin.resolve.substitutedUnderlyingTypes

class FixCallableMembersConflictsPhase(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) : SkieLinkingPhase {

    override fun execute() {
        skieModule.configure(SkieModule.Ordering.Last) {
            val allMembers = descriptorProvider.allExposedMembers.map { it.swiftModel }

            val sortedMembers = allMembers.sortedByCollisionResolutionPriority()

            buildUniqueSignatureSet(sortedMembers)
        }
    }

    context(SwiftModelScope)
    private fun List<MutableKotlinCallableMemberSwiftModel>.sortedByCollisionResolutionPriority(): List<MutableKotlinCallableMemberSwiftModel> =
        this.sortedByDescending { it.collisionResolutionPriority }

    /**
     * built in (is prioritized)
     * base vs inherited (base is prioritized)
     * true member vs extension (member is prioritized)
     * open vs final (open is prioritized)
     * property vs function (property is prioritized)
     * number of substituted types in parameter types (lower is better)
     * number of non-exported classes in parameter types (lower is better)
     * number of affected members (lower is better)
     * number of nested packages (lower is better)
     * length of fqname (lower is better)
     * hash of toString
     */
    private val KotlinCallableMemberSwiftModel.collisionResolutionPriority: Long
        get() {
            var priority = 0L

            if (this.descriptor.overriddenTreeAsSequence(true).any { it.fqNameSafe.asString().startsWith("kotlin.") }) {
                priority += 1
            }

            priority = priority shl 1
            if (this.descriptor.overriddenDescriptors.isEmpty()) {
                priority += 1
            }

            priority = priority shl 1
            if (this.descriptor.extensionReceiverParameter == null) {
                priority += 1
            }

            priority = priority shl 1
            if (this.descriptor.isOverridable) {
                priority += 1
            }

            priority = priority shl 1
            if (this is KotlinPropertySwiftModel) {
                priority += 1
            }

            priority = priority shl 4
            priority += 15 - this.descriptor.allParameters.sumOf { it.type.substitutedUnderlyingTypes().size }.coerceAtMost(15)

            priority = priority shl 5
            priority += 31 - this.descriptor.allParameters
                .count { parameterDescriptor ->
                    val parameterClassDescriptor = parameterDescriptor.type.constructor.declarationDescriptor as? ClassDescriptor

                    parameterClassDescriptor?.let { it in descriptorProvider.exposedClasses } != true
                }
                .coerceAtMost(31)

            priority = priority shl 5
            priority += 31 - this.allBoundedSwiftModels.size.coerceAtMost(31)

            priority = priority shl 5
            priority += 31 - this.descriptor.findPackage().fqName.pathSegments().size.coerceAtMost(31)

            priority = priority shl 5
            priority += 31 - this.descriptor.findPackage().fqName.asString().length.coerceAtMost(31)

            priority = priority shl 32
            // Drops unstable part of toString()
            priority += this.descriptor.toString().dropLastWhile { it != '@' }.hashCode()

            return priority
        }

    private fun buildUniqueSignatureSet(members: List<MutableKotlinCallableMemberSwiftModel>) {
        val signatureSet = UniqueSignatureSet()

        members.forEach { member ->
            signatureSet.addGroup(member)
        }
    }
}
