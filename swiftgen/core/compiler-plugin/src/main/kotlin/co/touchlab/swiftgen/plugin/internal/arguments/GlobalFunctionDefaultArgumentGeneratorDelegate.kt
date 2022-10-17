package co.touchlab.swiftgen.plugin.internal.arguments

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.irbuilder.DeclarationBuilder
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class GlobalFunctionDefaultArgumentGeneratorDelegate(
    declarationBuilder: DeclarationBuilder,
    swiftPackModuleBuilder: SwiftPackModuleBuilder,
    configuration: Configuration,
) : BaseFunctionDefaultArgumentGeneratorDelegate(declarationBuilder, swiftPackModuleBuilder, configuration) {

    override fun DescriptorProvider.allSupportedFunctions(): List<SimpleFunctionDescriptor> =
        this.topLevelCallableDescriptors
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { it.isSupported }
            .filter { it.canBeUsedWithExperimentalFeatures }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.extensionReceiverParameter == null &&
                this.contextReceiverParameters.isEmpty()
}
