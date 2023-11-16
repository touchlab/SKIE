package co.touchlab.skie.phases.memberconflicts

import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirEnumCase
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirSimpleFunction
import co.touchlab.skie.sir.element.getEntireOverrideHierarchy

class UniqueSignatureSet {

    private val alreadyAddedDeclarations = mutableSetOf<SirCallableDeclaration>()
    private val alreadyAddedEnumCase = mutableSetOf<SirEnumCase>()
    private val existingSignatures = mutableSetOf<Signature>()

    fun add(callableDeclaration: SirCallableDeclaration) {
        if (callableDeclaration in alreadyAddedDeclarations) {
            return
        }

        val group = Group(callableDeclaration)

        while (group.createsConflict) {
            group.mangle()
        }

        group.addToAlreadyAdded()
    }

    fun add(enumCase: SirEnumCase) {
        if (enumCase in alreadyAddedEnumCase) {
            return
        }

        var signature = enumCase.signature

        while (signature in existingSignatures) {
            enumCase.simpleName += "_"

            signature = enumCase.signature
        }

        existingSignatures.add(signature)
        alreadyAddedEnumCase.add(enumCase)
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
                                "shouldn't create conflicts (as they are processed first).",
                        )

                    lastValueParameter.label = lastValueParameter.labelOrName + "_"
                }
            }
        }

        fun addToAlreadyAdded() {
            alreadyAddedDeclarations.addAll(callableDeclarations)
            existingSignatures.addAll(signatures)
        }
    }
}
