package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftpack.api.SkieContext
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class GlobalFunctionDefaultArgumentGeneratorDelegate(
    skieContext: SkieContext,
    declarationBuilder: DeclarationBuilder,
    configuration: Configuration,
) : BaseFunctionDefaultArgumentGeneratorDelegate(skieContext, declarationBuilder, configuration) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.topLevelCallableDescriptors
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }
            .filter { it.canBeUsedWithExperimentalFeatures }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.extensionReceiverParameter == null &&
                this.contextReceiverParameters.isEmpty()
}
