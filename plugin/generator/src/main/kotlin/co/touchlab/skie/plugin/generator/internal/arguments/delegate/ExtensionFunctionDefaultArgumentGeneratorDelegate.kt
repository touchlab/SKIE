package co.touchlab.skie.plugin.generator.internal.arguments.delegate

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.SharedCounter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class ExtensionFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
    sharedCounter: SharedCounter,
) : BaseFunctionDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration, sharedCounter) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.exportedCategoryMembersCallableDescriptors
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.contextReceiverParameters.isEmpty() && !this.belongsToSkieRuntime
}
