package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.memberconflicts.UniqueSignatureSet.Collision.Group
import co.touchlab.skie.phases.memberconflicts.UniqueSignatureSet.Collision.RemoveExisting
import co.touchlab.skie.phases.memberconflicts.UniqueSignatureSet.Collision.RemoveNew
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.applyToEntireOverrideHierarchy
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy.Remove
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy.Rename
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel

class UniqueSignatureSet {

    private val alreadyAdded = mutableSetOf<KotlinCallableMemberSwiftModel>()
    private val signatureMap = mutableMapOf<Signature, MutableKotlinDirectlyCallableMemberSwiftModel>()
    private val removedSignaturesWithRemoveStrategy = mutableMapOf<Signature, Remove>()

    fun addGroup(representative: MutableKotlinCallableMemberSwiftModel) {
        if (representative in alreadyAdded) {
            return
        }
        alreadyAdded.addGroup(representative)

        while (true) {
            val collision = findHighestPriorityCollision(representative)

            if (collision != null) {
                collision.resolve(representative) { this.remove() }
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
        if (!this.model.isNotRemoved) return null

        if (this.needsToBeRemovedBecauseOfCollisionWithAlreadyRemovedSignature) return RemoveNew

        val existingModel = getNonRemovedModelForSignatureOrNull(this.signature) ?: return null

        return this.determineCollisionWithExposedDeclaration(existingModel)
    }

    private val DirectlyCallableSwiftModelWithSignature.needsToBeRemovedBecauseOfCollisionWithAlreadyRemovedSignature: Boolean
        get() {
            val collisionResolutionStrategy = this.model.collisionResolutionStrategy

            if (collisionResolutionStrategy !is Remove) return false
            val strategyOfConflictingRemovedSignature = removedSignaturesWithRemoveStrategy[this.signature] ?: return false

            return collisionResolutionStrategy.shouldBeRemovedBefore(strategyOfConflictingRemovedSignature)
        }

    private fun DirectlyCallableSwiftModelWithSignature.determineCollisionWithExposedDeclaration(
        existingModel: MutableKotlinDirectlyCallableMemberSwiftModel,
    ): Collision {
        val newModelStrategy = this.model.collisionResolutionStrategy
        val existingModelStrategy = existingModel.collisionResolutionStrategy

        return when (newModelStrategy) {
            Rename -> when (existingModelStrategy) {
                Rename -> Collision.Rename
                is Remove -> RemoveExisting(existingModel)
            }
            is Remove -> when (existingModelStrategy) {
                Rename -> RemoveNew
                is Remove -> Group(
                    listOfNotNull(
                        RemoveNew.takeIf { newModelStrategy.shouldBeRemovedBefore(existingModelStrategy) },
                        RemoveExisting(existingModel).takeIf { existingModelStrategy.shouldBeRemovedBefore(newModelStrategy) },
                    ),
                )
            }
        }
    }

    private val KotlinDirectlyCallableMemberSwiftModel.isNotRemoved: Boolean
        get() = this.kotlinSirCallableDeclaration.visibility != SirVisibility.Removed

    private fun getNonRemovedModelForSignatureOrNull(signature: Signature): MutableKotlinDirectlyCallableMemberSwiftModel? =
        signatureMap[signature]?.takeIf { it.isNotRemoved }

    private fun Remove.shouldBeRemovedBefore(other: Remove): Boolean =
        this.priority >= other.priority

    private fun addToSignatureMap(representative: MutableKotlinCallableMemberSwiftModel) {
        representative.allGroupMembersWithSignatures
            .filter { it.model.isNotRemoved }
            .distinctBy { it.signature }
            .forEach { (model, signature) ->
                check(getNonRemovedModelForSignatureOrNull(signature) == null) { "$model has signature ($signature) that is already added." }

                signatureMap[signature] = model
            }
    }

    private val MutableKotlinCallableMemberSwiftModel.allGroupMembersWithSignatures: List<DirectlyCallableSwiftModelWithSignature>
        get() = this.directlyCallableMembers
            .flatMap { it.allBoundedSwiftModels }
            .map { DirectlyCallableSwiftModelWithSignature(it, it.signature) }

    private fun MutableKotlinDirectlyCallableMemberSwiftModel.remove() {
        this.kotlinSirCallableDeclaration.visibility = SirVisibility.Removed

        this.addToRemovedSignaturesMap()
    }

    private fun MutableKotlinDirectlyCallableMemberSwiftModel.addToRemovedSignaturesMap() {
        val removeStrategy = this.collisionResolutionStrategy as? Remove ?: return

        val existingRemovedSignature = removedSignaturesWithRemoveStrategy[this.signature]

        if (existingRemovedSignature == null || existingRemovedSignature.shouldBeRemovedBefore(removeStrategy)) {
            removedSignaturesWithRemoveStrategy[this.signature] = removeStrategy
        }
    }

    private data class DirectlyCallableSwiftModelWithSignature(
        val model: MutableKotlinDirectlyCallableMemberSwiftModel,
        val signature: Signature,
    )

    private interface Collision {

        val priority: Int

        fun resolve(representative: MutableKotlinCallableMemberSwiftModel, remove: MutableKotlinDirectlyCallableMemberSwiftModel.() -> Unit)

        object Rename : Collision {

            override val priority: Int = 1

            override fun resolve(
                representative: MutableKotlinCallableMemberSwiftModel,
                remove: MutableKotlinDirectlyCallableMemberSwiftModel.() -> Unit,
            ) {
                representative.accept(RenameCollisionVisitor)
            }

            object RenameCollisionVisitor : MutableKotlinCallableMemberSwiftModelVisitor.Unit {

                override fun visit(function: MutableKotlinFunctionSwiftModel) {
                    when (function.role) {
                        KotlinFunctionSwiftModel.Role.Constructor -> {
                            val lastParameter = function.kotlinSirConstructor.valueParameters.lastOrNull()
                                ?: error("Class ${function.receiver} has multiple constructors without parameters.")

                            lastParameter.label = lastParameter.labelOrName + "_"
                        }
                        else -> function.kotlinSirFunction.applyToEntireOverrideHierarchy {
                            identifier += "_"
                        }
                    }
                }

                override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
                    regularProperty.kotlinSirProperty.applyToEntireOverrideHierarchy {
                        identifier += "_"
                    }
                }
            }
        }

        class RemoveExisting(private val oldConflictingMember: MutableKotlinDirectlyCallableMemberSwiftModel) : Collision {

            override val priority: Int = 0

            override fun resolve(
                representative: MutableKotlinCallableMemberSwiftModel,
                remove: MutableKotlinDirectlyCallableMemberSwiftModel.() -> Unit,
            ) {
                oldConflictingMember.remove()
            }
        }

        object RemoveNew : Collision {

            override val priority: Int = 0

            override fun resolve(
                representative: MutableKotlinCallableMemberSwiftModel,
                remove: MutableKotlinDirectlyCallableMemberSwiftModel.() -> Unit,
            ) {
                representative.directlyCallableMembers.forEach {
                    it.remove()
                }
            }
        }

        class Group(val collisions: List<Collision>) : Collision {

            override val priority: Int = collisions.maxOf { it.priority }

            override fun resolve(
                representative: MutableKotlinCallableMemberSwiftModel,
                remove: MutableKotlinDirectlyCallableMemberSwiftModel.() -> Unit,
            ) {
                collisions.forEach {
                    it.resolve(representative, remove)
                }
            }
        }
    }
}
