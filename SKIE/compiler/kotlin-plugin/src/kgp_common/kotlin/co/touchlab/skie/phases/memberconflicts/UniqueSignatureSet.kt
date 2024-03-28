package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.getEntireOverrideHierarchy
import co.touchlab.skie.util.resolveCollisionWithWarning

class UniqueSignatureSet {

    private val alreadyAddedDeclarations = mutableSetOf<SirCallableDeclaration>()
    private val alreadyAddedEnumCase = mutableSetOf<SirEnumCase>()

    // Map so that we can get the signatures for conflicts
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

            if (signature in existingSignaturesMap) {
                "an another declaration '${existingSignaturesMap[signature]}'"
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

            if (signature in existingSignaturesMap) {
                "an another declaration '${existingSignaturesMap[signature]}'"
            } else {
                null
            }
        }

        val signature = enumCase.signature
        existingSignaturesMap.putIfAbsent(signature, signature)
        alreadyAddedEnumCase.add(enumCase)
    }

    private inner class Group(
        private val representative: SirCallableDeclaration,
    ) {

        private val callableDeclarations = representative.getEntireOverrideHierarchy()

        context(SirPhase.Context)
        fun resolveCollisionWithWarning(collisionReasonProvider: SirCallableDeclaration.() -> String?) {
            do {
                var changed = false

                callableDeclarations.forEach {
                    // Avoid short-circuiting
                    changed = it.resolveCollisionWithWarning(collisionReasonProvider) || changed

                    unifyNames(it)
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
            val representativeSignature = representative.signature
            existingSignaturesMap.putIfAbsent(representativeSignature, representativeSignature)

            alreadyAddedDeclarations.addAll(callableDeclarations)
            callableDeclarations.forEach {
                val signature = it.signature

                existingSignaturesMap.putIfAbsent(signature, signature)
            }
        }
    }

    private val SirCallableDeclaration.signature: Signature
        get() = Signature(this@signature, sirHierarchyCache)

    private val SirEnumCase.signature: Signature
        get() = Signature(this@signature, sirHierarchyCache)
}
