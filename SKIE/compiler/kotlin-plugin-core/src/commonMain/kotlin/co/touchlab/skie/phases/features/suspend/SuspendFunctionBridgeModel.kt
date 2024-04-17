package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirSimpleFunction

data class SuspendFunctionBridgeModel(
    val suspendKirFunction: KirSimpleFunction,
    val kotlinBridgingKirFunction: KirSimpleFunction,
) {

    val originalFunction: SirSimpleFunction =
        suspendKirFunction.bridgedSirFunction ?: error("Suspend function $suspendKirFunction does not have an async bridge.")

    val suspendKirFunctionAssociatedDeclarations: List<SirSimpleFunction> =
        // TODO Change after bridged declarations are replaced with async function
        listOfNotNull(suspendKirFunction.originalSirFunction, suspendKirFunction.bridgedSirFunction)

    val kotlinBridgingFunction: SirSimpleFunction = kotlinBridgingKirFunction.originalSirFunction

    val kotlinBridgingFunctionOwner: SirClass = kotlinBridgingKirFunction.owner.originalSirClass

    val suspendFunctionOwner: KirClass = suspendKirFunction.owner

    val isFromGenericClass: Boolean = this.suspendKirFunction.owner.typeParameters.isEmpty().not()

    // Can be called only during code generation
    val isFromBridgedClass: Boolean
        get() = suspendFunctionOwner.bridgedSirClass != null
}
