package co.touchlab.skie.phases.features.defaultarguments.delegate

import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.util.SharedCounter
import co.touchlab.skie.kir.irbuilder.DeclarationBuilder
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class TopLevelFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    descriptorProvider: DescriptorProvider,
    declarationBuilder: DeclarationBuilder,
    sharedCounter: SharedCounter,
) : BaseFunctionDefaultArgumentGeneratorDelegate(
    skieContext = skieContext,
    descriptorProvider = descriptorProvider,
    declarationBuilder = declarationBuilder,
    sharedCounter = sharedCounter,
) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.exposedTopLevelMembers
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty()
}
