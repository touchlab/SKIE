package co.touchlab.skie.phases.sir.member

import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.KirValueParameter
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.features.suspend.isSuspendInteropEnabled
import co.touchlab.skie.sir.element.copyValueParametersFrom
import co.touchlab.skie.sir.element.shallowCopy

object CreateAsyncSirFunctionsPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        kirProvider.allSimpleFunctions
            .filter { it.isSuspend && it.isSuspendInteropEnabled }
            .forEach {
                createAsyncFunction(it)
            }
    }

    context(SirPhase.Context)
    private fun createAsyncFunction(function: KirSimpleFunction) {
        val originalSirFunction = function.originalSirFunction

        val suspendCompletionParameter = function.valueParameters.single { it.kind is KirValueParameter.Kind.SuspendCompletion }

        function.bridgedSirFunction = originalSirFunction.shallowCopy(
            returnType = sirTypeTranslator.mapSuspendCompletionType(suspendCompletionParameter.oirValueParameter.type),
            isAsync = true,
            throws = true,
        ).apply {
            val valueParametersWithoutSuspendCompletion = (function.valueParameters - suspendCompletionParameter)
                .mapNotNull { it.oirValueParameter.originalSirValueParameter }

            copyValueParametersFrom(valueParametersWithoutSuspendCompletion)
        }
    }
}
