package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.InternalDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SharedCounter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class ExtensionFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    descriptorProvider: InternalDescriptorProvider,
    declarationBuilder: DeclarationBuilder,
    sharedCounter: SharedCounter,
) : BaseFunctionDefaultArgumentGeneratorDelegate(
    skieContext = skieContext,
    descriptorProvider = descriptorProvider,
    declarationBuilder = declarationBuilder,
    sharedCounter = sharedCounter
) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.exposedCategoryMembers
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty()
}
