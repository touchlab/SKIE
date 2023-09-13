package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.util.SharedCounter
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

class TopLevelFunctionDefaultArgumentGeneratorDelegate(
    context: DescriptorModificationPhase.Context,
    sharedCounter: SharedCounter,
) : BaseFunctionDefaultArgumentGeneratorDelegate(context, sharedCounter) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.exposedTopLevelMembers
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty()
}
