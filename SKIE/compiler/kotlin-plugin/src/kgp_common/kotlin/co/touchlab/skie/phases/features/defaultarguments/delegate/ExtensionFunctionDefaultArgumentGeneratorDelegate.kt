package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.util.SharedCounter
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

class ExtensionFunctionDefaultArgumentGeneratorDelegate(
    context: DescriptorModificationPhase.Context,
    sharedCounter: SharedCounter,
) : BaseFunctionDefaultArgumentGeneratorDelegate(context, sharedCounter) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.exposedCategoryMembers
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty()
}
