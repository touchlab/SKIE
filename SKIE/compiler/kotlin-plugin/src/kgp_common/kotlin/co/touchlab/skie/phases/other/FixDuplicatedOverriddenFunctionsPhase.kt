package co.touchlab.skie.phases.other

import co.touchlab.skie.oir.element.OirSimpleFunction
import co.touchlab.skie.oir.element.copyValueParametersFrom
import co.touchlab.skie.oir.element.memberSimpleFunctions
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.shallowCopy

// Fix for SKIE-395.
// The implementation is somewhat hacky because it creates a fake function that is used to remove the duplicates.
// This fake function is not properly linked with the overrides and as a result this phase needs to run after all other phases that rename functions.
object FixDuplicatedOverriddenFunctionsPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        oirProvider.allKotlinClassesAndProtocols
            .flatMap { it.memberSimpleFunctions }
            .forEach {
                fixDuplicates(it)
            }
    }

    private fun fixDuplicates(function: OirSimpleFunction) {
        val allVariants = function.allUniqueVariants
        if (allVariants.size <= 1) {
            return
        }

        val mainVariant = allVariants.single { it.selector == function.selector }
        val otherVariants = allVariants - mainVariant

        otherVariants.forEach {
            createInaccessibleOverride(function, it)
        }
    }

    private fun createInaccessibleOverride(function: OirSimpleFunction, baseFunctionToOverride: OirSimpleFunction) {
        val override = OirSimpleFunction(
            selector = baseFunctionToOverride.selector,
            parent = function.parent,
            scope = function.scope,
            returnType = function.returnType,
            errorHandlingStrategy = function.errorHandlingStrategy,
            deprecationLevel = function.deprecationLevel,
        )

        override.copyValueParametersFrom(baseFunctionToOverride)

        override.originalSirFunction = baseFunctionToOverride.originalSirFunction.shallowCopy(
            parent = function.originalSirFunction.parent,
            visibility = SirVisibility.Private,
        ).apply {
            copyValueParametersFrom(baseFunctionToOverride.originalSirFunction)
        }
    }

    private val OirSimpleFunction.allUniqueVariants: List<OirSimpleFunction>
        get() {
            val baseFunctions = overriddenDeclarations.flatMap { it.allUniqueVariants }

            return when (baseFunctions.size) {
                0 -> listOf(this)
                1 -> baseFunctions
                else -> {
                    val uniqueSelectors = baseFunctions.map { it.selector }.distinct()

                    return uniqueSelectors.map { selector -> baseFunctions.first { it.selector == selector } }
                }
            }
        }
}
