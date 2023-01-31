package co.touchlab.skie.api.apinotes.fixes.memberconflicts

import co.touchlab.skie.api.apinotes.fixes.memberconflicts.UniqueSignatureSet.Collision.RemoveExisting
import co.touchlab.skie.api.apinotes.fixes.memberconflicts.UniqueSignatureSet.Collision.RemoveNew
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy.Remove
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy.Rename
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.isRemoved

class UniqueSignatureSet {

    private val alreadyAdded = mutableSetOf<KotlinCallableMemberSwiftModel>()
    private val signatureMap = mutableMapOf<Signature, MutableKotlinDirectlyCallableMemberSwiftModel>()

    fun addGroup(representative: MutableKotlinCallableMemberSwiftModel) {
        if (representative in alreadyAdded) {
            return
        }
        alreadyAdded.addGroup(representative)

        while (true) {
            val collision = findHighestPriorityCollision(representative)

            if (collision != null) {
                collision.resolve(representative)
            } else {
                addToSignatureMap(representative)
                return
            }
        }
    }

    private fun MutableSet<KotlinCallableMemberSwiftModel>.addGroup(representative: MutableKotlinCallableMemberSwiftModel) {
        val allGroupMembers = representative.directlyCallableMembers
            .flatMap { it.directlyCallableMembers + it }
            .flatMap { it.allBoundedSwiftModels }

        this.addAll(allGroupMembers)
    }

    private fun findHighestPriorityCollision(representative: MutableKotlinCallableMemberSwiftModel): Collision? =
        representative.allGroupMembersWithSignatures
            .mapNotNull { it.findCollisionIfExists() }
            .maxByOrNull { it.priority }

    private fun DirectlyCallableSwiftModelWithSignature.findCollisionIfExists(): Collision? {
        val existingModel = signatureMap[this.signature] ?: return null

        if (this.model.visibility.isRemoved) return null

        val newModelStrategy = this.model.collisionResolutionStrategy
        val existingModelStrategy = existingModel.collisionResolutionStrategy

        return if (!existingModel.visibility.isRemoved) {
            when (newModelStrategy) {
                is Remove -> when (existingModelStrategy) {
                    is Remove -> if (existingModelStrategy.hasRemovePriority(newModelStrategy)) RemoveExisting(existingModel) else RemoveNew
                    Rename -> RemoveNew
                }
                Rename -> when (existingModelStrategy) {
                    is Remove -> RemoveExisting(existingModel)
                    Rename -> Collision.Rename
                }
            }
        } else {
            when (newModelStrategy) {
                is Remove -> when (existingModelStrategy) {
                    is Remove -> if (newModelStrategy.hasRemovePriority(existingModelStrategy)) RemoveNew else null
                    Rename -> null
                }
                Rename -> null
            }
        }
    }

    private fun Remove.hasRemovePriority(other: Remove): Boolean =
        this.priority >= other.priority

    private fun addToSignatureMap(representative: MutableKotlinCallableMemberSwiftModel) {
        representative.allGroupMembersWithSignatures.forEach {
            addToSignatureMap(it)
        }
    }

    private fun addToSignatureMap(newModelWithSignature: DirectlyCallableSwiftModelWithSignature) {
        fun putNewModelIntoSignatureMap() {
            signatureMap[newModelWithSignature.signature] = newModelWithSignature.model
        }

        val existingModel = signatureMap[newModelWithSignature.signature]
        val newModel = newModelWithSignature.model

        when {
            existingModel == null -> putNewModelIntoSignatureMap()
            !existingModel.visibility.isRemoved -> check(newModel.visibility.isRemoved || newModel in existingModel.allBoundedSwiftModels)
            !newModel.visibility.isRemoved -> putNewModelIntoSignatureMap()
            else -> {
                val newModelStrategy = newModelWithSignature.model.collisionResolutionStrategy
                val existingModelStrategy = existingModel.collisionResolutionStrategy

                check(newModelStrategy is Remove)
                check(existingModelStrategy is Remove)

                if (existingModelStrategy.hasRemovePriority(newModelStrategy)) {
                    putNewModelIntoSignatureMap()
                }
            }
        }
    }

    private val MutableKotlinCallableMemberSwiftModel.allGroupMembersWithSignatures: List<DirectlyCallableSwiftModelWithSignature>
        get() = this.directlyCallableMembers
            .flatMap { it.allBoundedSwiftModels }
            .map { DirectlyCallableSwiftModelWithSignature(it, it.signature) }

    private data class DirectlyCallableSwiftModelWithSignature(
        val model: MutableKotlinDirectlyCallableMemberSwiftModel,
        val signature: Signature,
    )

    private interface Collision {

        val priority: Int

        fun resolve(representative: MutableKotlinCallableMemberSwiftModel)

        object Rename : Collision {

            override val priority: Int = 2

            override fun resolve(representative: MutableKotlinCallableMemberSwiftModel) {
                representative.accept(RenameCollisionVisitor)
            }

            object RenameCollisionVisitor : MutableKotlinCallableMemberSwiftModelVisitor.Unit {

                override fun visit(function: MutableKotlinFunctionSwiftModel) {
                    when (function.role) {
                        KotlinFunctionSwiftModel.Role.Constructor -> {
                            val lastParameter = function.parameters.lastOrNull()
                                ?: error("Class ${function.receiver} has multiple constructors without parameters.")

                            lastParameter.argumentLabel += "_"
                        }
                        else -> function.identifier += "_"
                    }
                }

                override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
                    regularProperty.identifier += "_"
                }
            }
        }

        class RemoveExisting(private val oldConflictingMember: MutableKotlinDirectlyCallableMemberSwiftModel) : Collision {

            override val priority: Int = 1

            override fun resolve(representative: MutableKotlinCallableMemberSwiftModel) {
                oldConflictingMember.visibility = SwiftModelVisibility.Removed
            }
        }

        object RemoveNew : Collision {

            // Old models must be removed first (to make sure they are all removed), only then can be the new models removed.
            override val priority: Int = 0

            override fun resolve(representative: MutableKotlinCallableMemberSwiftModel) {
                representative.directlyCallableMembers.forEach {
                    it.visibility = SwiftModelVisibility.Removed
                }
            }
        }
    }
}
