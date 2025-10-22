package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.getEntireOverrideHierarchy
import co.touchlab.skie.sir.signature.Signature
import co.touchlab.skie.sir.signature.SirHierarchyCache
import co.touchlab.skie.util.resolveCollisionWithWarning

class UniqueSignatureSet {

    private val alreadyAddedDeclarations = mutableSetOf<SirCallableDeclaration>()
    private val alreadyAddedEnumCase = mutableSetOf<SirEnumCase>()

    // Map so that we can get the signatures for conflicts by identifier (for performance reasons)
    // Map of signature to signature instead of set because we need to know which declaration is conflicting
    // The algorithm utilises custom equality which checks if the signature is the same from the overload resolution perspective.
    private val existingSignaturesMap = mutableMapOf<Signature, Signature>()

    private val sirHierarchyCache = SirHierarchyCache()

    context(SirPhase.Context)
    fun add(callableDeclaration: SirCallableDeclaration) {
        if (callableDeclaration in alreadyAddedDeclarations) {
            return
        }

        val group = Group(callableDeclaration)

        group.resolveCollisionWithWarning {
            val signature = signature

            val conflictingSignature = findConflictingSignature(signature)

            if (conflictingSignature != null) {
                "an another declaration '$conflictingSignature'"
            } else {
                null
            }
        }

        group.addToCaches()
    }

    context(SirPhase.Context)
    fun add(enumCase: SirEnumCase) {
        if (enumCase in alreadyAddedEnumCase) {
            return
        }

        enumCase.resolveCollisionWithWarning {
            val signature = signature

            val conflictingSignature = findConflictingSignature(signature)

            if (conflictingSignature != null) {
                "an another declaration '${conflictingSignature}'"
            } else {
                null
            }
        }

        addSignature(enumCase.signature)

        alreadyAddedEnumCase.add(enumCase)
    }

    private fun findConflictingSignature(signature: Signature): Signature? =
        existingSignaturesMap[signature]

    private fun addSignature(signature: Signature) {
        existingSignaturesMap.putIfAbsent(signature, signature)
    }

    private inner class Group(
        private val representative: SirCallableDeclaration,
    ) {

        private val callableDeclarations = representative.getEntireOverrideHierarchy()

        context(SirPhase.Context)
        fun resolveCollisionWithWarning(collisionReasonProvider: SirCallableDeclaration.() -> String?) {
            do {
                var changed = false

                for (callableDeclaration in callableDeclarations) {
                    changed = callableDeclaration.resolveCollisionWithWarning(collisionReasonProvider)

                    if (changed) {
                        unifyNames(callableDeclaration)

                        break
                    }
                }
            } while (changed)
        }

        private fun unifyNames(basedOn: SirCallableDeclaration) {
            callableDeclarations.forEach {
                when (it) {
                    is SirSimpleFunction -> it.identifier = basedOn.identifier
                    is SirProperty -> it.identifier = basedOn.identifier
                    is SirConstructor -> {
                        it.valueParameters.lastOrNull()?.label = (basedOn as SirConstructor).valueParameters.lastOrNull()?.label
                    }
                }
            }
        }

        fun addToCaches() {
            addSignature(representative.signature)

            alreadyAddedDeclarations.addAll(callableDeclarations)

            callableDeclarations.forEach {
                addSignature(it.signature)
            }
        }
    }

    private val SirCallableDeclaration.signature: Signature
        get() = Signature(this@signature, sirHierarchyCache)

    private val SirEnumCase.signature: Signature
        get() = Signature(this@signature, sirHierarchyCache)
}
