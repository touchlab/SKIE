package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.util.doInPhase
import org.jetbrains.kotlin.descriptors.FunctionDescriptor

class SwiftSuspendGeneratorDelegate(
    private val context: DescriptorModificationPhase.Context,
) {

    private val swiftSuspendFunctionGenerator = SwiftSuspendFunctionGenerator()

    fun generateSwiftBridgingFunction(
        originalFunctionDescriptor: FunctionDescriptor,
        kotlinBridgingFunctionDescriptor: FunctionDescriptor,
    ) {
        context.doInPhase(SuspendGenerator.SwiftBridgeGeneratorInitPhase) {
            val suspendKirFunction = descriptorKirProvider.getFunction(originalFunctionDescriptor)
            val kotlinBridgingKirFunction = descriptorKirProvider.getFunction(kotlinBridgingFunctionDescriptor)

            doInPhase(SuspendGenerator.SwiftBridgeGeneratorFinalizePhase) {
                swiftSuspendFunctionGenerator.generateSwiftBridgingFunction(suspendKirFunction, kotlinBridgingKirFunction)
            }
        }
    }
}
