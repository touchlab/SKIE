package co.touchlab.skie.api.apinotes.fixes

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.module.SkieModule
import org.jetbrains.kotlin.backend.common.descriptors.allParameters
import org.jetbrains.kotlin.backend.common.serialization.findPackage
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isOverridable
import org.jetbrains.kotlin.resolve.substitutedUnderlyingTypes
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

class CallableMembersConflictsApiNotesFix(
    private val skieModule: SkieModule,
    private val descriptorProvider: DescriptorProvider,
) {

    fun fixNames() {
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

            priority = priority shl 5
            priority += 31 - this.descriptor.allParameters.sumOf { it.type.substitutedUnderlyingTypes().size }.coerceAtMost(31)

            priority = priority shl 5
            priority += 31 - this.descriptor.allParameters
                .count { parameterDescriptor ->
                    val parameterClassDescriptor = parameterDescriptor.type.constructor.declarationDescriptor as? ClassDescriptor

                    parameterClassDescriptor?.let { descriptorProvider.isExposed(it) } != true
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
        val alreadyAddedMembers = mutableSetOf<MutableKotlinCallableMemberSwiftModel>()

        members.forEach { member ->
            if (member !in alreadyAddedMembers) {
                val allBoundedModels = member.allBoundedSwiftModels

                signatureSet.addAll(allBoundedModels)
                alreadyAddedMembers.addAll(allBoundedModels)
            }
        }
    }

    private fun UniqueSignatureSet.addAll(members: List<MutableKotlinCallableMemberSwiftModel>) {
        while (true) {
            val signatures = members.flatMap { it.accept(SignatureConvertorVisitor) }

            this.tryAddAll(signatures).ifTrue { return }

            members.first().accept(TryToFixSignatureCollisionVisitor)
        }
    }

    private object SignatureConvertorVisitor : KotlinCallableMemberSwiftModelVisitor<List<Signature>> {

        override fun visit(function: KotlinFunctionSwiftModel): List<Signature> =
            listOf(
                Signature(
                    receiver = function.receiver.stableFqName,
                    identifier = function.identifier,
                    parameters = function.parameters.map { it.toSignatureParameter() },
                    returnType = Signature.ReturnType.Specific(function.returnType.stableFqName),
                )
            )

        private fun KotlinParameterSwiftModel.toSignatureParameter(): Signature.Parameter =
            Signature.Parameter(
                argumentLabel = this.argumentLabel,
                type = this.type.stableFqName,
            )

        override fun visit(regularProperty: KotlinRegularPropertySwiftModel): List<Signature> =
            listOf(
                Signature(
                    receiver = regularProperty.receiver.stableFqName,
                    identifier = regularProperty.identifier,
                    parameters = emptyList(),
                    returnType = Signature.ReturnType.Any,
                )
            )

        override fun visit(convertedProperty: KotlinConvertedPropertySwiftModel): List<Signature> =
            convertedProperty.accessors.flatMap { visit(it) }
    }

    private object TryToFixSignatureCollisionVisitor : MutableKotlinCallableMemberSwiftModelVisitor.Unit {

        override fun visit(function: MutableKotlinFunctionSwiftModel) {
            when (function.kind) {
                KotlinFunctionSwiftModel.Kind.Constructor -> {
                    val lastParameter = function.parameters.lastOrNull()
                        ?: error("Constructor $function without parameters can never create a collision.")

                    lastParameter.argumentLabel += "_"
                }
                else -> function.identifier += "_"
            }
        }

        override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
            regularProperty.identifier += "_"
        }

        override fun visit(convertedProperty: MutableKotlinConvertedPropertySwiftModel) {
            visit(convertedProperty.getter)
        }
    }

    private data class Signature(
        val receiver: String,
        val identifier: String,
        val parameters: List<Parameter>,
        val returnType: ReturnType,
    ) {

        data class Parameter(val argumentLabel: String, val type: String)

        sealed interface ReturnType {

            class Specific(val name: String) : ReturnType {

                override fun equals(other: kotlin.Any?): Boolean {
                    if (this === other) return true
                    if (other is Any) return true
                    if (javaClass != other?.javaClass) return false

                    other as Specific

                    if (name != other.name) return false

                    return true
                }

                override fun hashCode(): Int = 0
            }

            object Any : ReturnType {

                override fun equals(other: kotlin.Any?): Boolean {
                    if (other is ReturnType) return true

                    return super.equals(other)
                }

                override fun hashCode(): Int = 0
            }
        }
    }

    private class UniqueSignatureSet {

        private val existingSignatures = mutableSetOf<Signature>()

        fun tryAddAll(signatures: Collection<Signature>): Boolean {
            val alreadyExists = signatures.any { it in existingSignatures }

            if (!alreadyExists) {
                existingSignatures.addAll(signatures)
            }

            return !alreadyExists
        }
    }
}
