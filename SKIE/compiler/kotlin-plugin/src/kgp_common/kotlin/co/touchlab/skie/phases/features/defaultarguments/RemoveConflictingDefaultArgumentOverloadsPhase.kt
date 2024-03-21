package co.touchlab.skie.phases.features.defaultarguments

import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.memberconflicts.Signature
import co.touchlab.skie.phases.memberconflicts.signature
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.getEntireOverrideHierarchy
import co.touchlab.skie.sir.element.isExported

object RemoveConflictingDefaultArgumentOverloadsPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val allBaseFunctions = kirProvider.allConstructors + kirProvider.allSimpleFunctions.filter { it.overriddenDeclarations.isEmpty() }

        val allDefaultArgumentOverloads = allBaseFunctions.flatMap { it.defaultArgumentsOverloads }.toSet()

        val allNonOverloads = allBaseFunctions - allDefaultArgumentOverloads

        val functionsWithOverloadsHierarchy = allNonOverloads.map { FunctionWithOverloadsHierarchy(it) }

        val uniqueSignatureSet = UniqueSignatureSet()

        functionsWithOverloadsHierarchy
            .flatMap { it.overloadHierarchies }
            .sortedBy { it.numberOfSkippedDefaultArguments }
            .forEach {
                uniqueSignatureSet.add(it)
            }
    }

    private class UniqueSignatureSet {

        private val signaturesToDeclaration = mutableMapOf<Signature, SirCallableDeclaration>()
        private val signaturesToFunctionHierarchy = mutableMapOf<Signature, FunctionHierarchy>()

        fun add(functionHierarchy: FunctionHierarchy) {
            functionHierarchy.resolveConflictIfExists()

            addToVisited(functionHierarchy)
        }

        private fun FunctionHierarchy.resolveConflictIfExists() {
            val hasConflict = allDeclarationsWithSignature.any { it.createsConflict }

            if (hasConflict) {
                this.resolveConflict()
            }
        }

        private fun FunctionHierarchy.resolveConflict() {
            this.removeIfOverload()

            allDeclarationsWithSignature.forEach {
                it.removeConflictingFunctionHierarchyIfNeeded(this.numberOfSkippedDefaultArguments)
            }
        }

        private fun DeclarationWithSignature.removeConflictingFunctionHierarchyIfNeeded(numberOfSkippedDefaultArguments: Int) {
            val conflictingFunctionHierarchy = signaturesToFunctionHierarchy[this.signature] ?: return

            if (conflictingFunctionHierarchy.numberOfSkippedDefaultArguments == numberOfSkippedDefaultArguments) {
                conflictingFunctionHierarchy.removeIfOverload()
            }
        }

        private val DeclarationWithSignature.createsConflict: Boolean
            get() = signaturesToDeclaration[this.signature]?.let { it != this.declaration } ?: false

        private fun addToVisited(functionHierarchy: FunctionHierarchy) {
            functionHierarchy.allDeclarationsWithSignature.forEach {
                signaturesToDeclaration[it.signature] = it.declaration
                signaturesToFunctionHierarchy[it.signature] = functionHierarchy
            }
        }
    }

    private class FunctionWithOverloadsHierarchy(
        representative: KirFunction<*>,
    ) {

        private val baseNumberOfSirValueParameters: Int = representative.originalSirDeclaration.valueParameters.size

        val overloadHierarchies = (representative.defaultArgumentsOverloads + representative)
            .map { it.originalSirDeclaration }
            .filter { it.isExported }
            .map {
                FunctionHierarchy(
                    representative = it,
                    numberOfSkippedDefaultArguments = baseNumberOfSirValueParameters - it.valueParameters.size,
                )
            }
    }

    private class FunctionHierarchy(
        representative: SirFunction,
        val numberOfSkippedDefaultArguments: Int,
    ) {

        private val overrideHierarchy = representative.getEntireOverrideHierarchy()

        val allDeclarationsWithSignature = overrideHierarchy.map { DeclarationWithSignature(it, it.signature) }

        init {
            check(numberOfSkippedDefaultArguments >= 0) {
                "numberOfSkippedDefaultArguments must be >= 0. Was: $numberOfSkippedDefaultArguments " +
                    "This is likely caused by passing one of the overloads instead of original function to FunctionWithOverloadsHierarchy."
            }
        }

        fun removeIfOverload() {
            if (numberOfSkippedDefaultArguments == 0) {
                return
            }

            overrideHierarchy.forEach {
                it.visibility = SirVisibility.Removed
            }
        }
    }

    private class DeclarationWithSignature(
        val declaration: SirCallableDeclaration,
        val signature: Signature,
    )
}
