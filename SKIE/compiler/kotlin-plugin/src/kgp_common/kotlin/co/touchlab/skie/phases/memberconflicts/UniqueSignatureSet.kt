package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.getEntireOverrideHierarchy

class UniqueSignatureSet {

    private val alreadyAdded = mutableSetOf<SirCallableDeclaration>()
    private val existingSignatures = mutableSetOf<Signature>()

    fun add(callableDeclaration: SirCallableDeclaration) {
        if (callableDeclaration in alreadyAdded) {
            return
        }

        val group = Group(callableDeclaration)

        while (group.createsConflict) {
            group.mangle()
        }

        group.addToAlreadyAdded()
    }

    private inner class Group(
        representative: SirCallableDeclaration,
    ) {

        private val callableDeclarations = representative.getEntireOverrideHierarchy()

        private var signatures = callableDeclarations.map { it.signature }

        val createsConflict: Boolean
            get() = signatures.any { it in existingSignatures }

        fun mangle() {
            callableDeclarations.forEach {
                it.mangle()
            }

            signatures = callableDeclarations.map { it.signature }
        }

        private fun SirCallableDeclaration.mangle() {
            when (this) {
                is SirSimpleFunction -> this.identifier += "_"
                is SirProperty -> this.identifier += "_"
                is SirConstructor -> {
                    val lastValueParameter = this.valueParameters.lastOrNull()
                        ?: error(
                            "Cannot mangle $this because it does not have any value parameters. " +
                                    "This should never happen because constructors without value parameters " +
                                    "shouldn't create conflicts (as they are processed first)."
                        )

                    lastValueParameter.label = lastValueParameter.labelOrName + "_"
                }
            }
        }

        fun addToAlreadyAdded() {
            alreadyAdded.addAll(callableDeclarations)
            existingSignatures.addAll(signatures)
        }
    }
}
