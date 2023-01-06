@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class SuspendGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
    configuration: Configuration,
    private val declarationBuilder: DeclarationBuilder,
) : BaseGenerator(skieContext, namespaceProvider, configuration) {

    override val isActive: Boolean = SkieFeature.SuspendInterop in configuration.enabledFeatures &&
        SkieFeature.SwiftRuntime in configuration.enabledFeatures

    override fun execute(descriptorProvider: NativeDescriptorProvider) {
        val kotlinDelegate = KotlinSuspendGeneratorDelegate(module, declarationBuilder, descriptorProvider)
        val swiftDelegate = SwiftSuspendGeneratorDelegate(module)

        descriptorProvider.allSupportedFunctions(descriptorProvider).forEach { function ->
            val kotlinBridgingFunction = kotlinDelegate.generateKotlinBridgingFunction(function)

            swiftDelegate.generateSwiftBridgingFunction(function, kotlinBridgingFunction)

            markOriginalFunctionAsReplaced(function)
        }
    }

    private fun DescriptorProvider.allSupportedFunctions(descriptorProvider: NativeDescriptorProvider): List<SimpleFunctionDescriptor> =
        this.allFunctions(descriptorProvider).filter { it.isSupported }

    private fun DescriptorProvider.allFunctions(descriptorProvider: NativeDescriptorProvider): List<SimpleFunctionDescriptor> =
        this.exportedTopLevelCallableDescriptors.filterIsInstance<SimpleFunctionDescriptor>() +
            this.exportedClassDescriptors.flatMap { it.allDeclaredMethods(descriptorProvider) }

    private fun ClassDescriptor.allDeclaredMethods(descriptorProvider: NativeDescriptorProvider): List<SimpleFunctionDescriptor> =
        this.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { descriptorProvider.mapper.isBaseMethod(it) }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.isSuspend && !this.belongsToSkieRuntime && this.extensionReceiverParameter == null

    private fun markOriginalFunctionAsReplaced(originalFunctionDescriptor: SimpleFunctionDescriptor) {
        module.configure {
            originalFunctionDescriptor.swiftModel.visibility = SwiftModelVisibility.Replaced
        }
    }
}

