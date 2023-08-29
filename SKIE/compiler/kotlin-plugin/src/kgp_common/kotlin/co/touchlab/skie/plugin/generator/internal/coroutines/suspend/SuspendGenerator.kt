@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.coroutines.suspend

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.SuspendInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.allExposedMembers
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.generator.internal.util.BaseGenerator
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.DeclarationBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

internal class SuspendGenerator(
    skieContext: SkieContext,
    namespaceProvider: NamespaceProvider,
    private val descriptorProvider: MutableDescriptorProvider,
    private val declarationBuilder: DeclarationBuilder,
) : BaseGenerator(skieContext, namespaceProvider) {

    override val isActive: Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    override fun runObjcPhase() {
        val kotlinDelegate = KotlinSuspendGeneratorDelegate(module, declarationBuilder, descriptorProvider)
        val swiftDelegate = SwiftSuspendGeneratorDelegate(module)

        descriptorProvider.allSupportedFunctions.forEach { function ->
            val kotlinBridgingFunction = kotlinDelegate.generateKotlinBridgingFunction(function)

            swiftDelegate.generateSwiftBridgingFunction(function, kotlinBridgingFunction)

            markOriginalFunctionAsReplaced(function)
        }
    }

    private val MutableDescriptorProvider.allSupportedFunctions: List<SimpleFunctionDescriptor>
        get() = this.allExposedMembers.filterIsInstance<SimpleFunctionDescriptor>()
            .filter { this.isBaseMethod(it) }
            .filter { it.isSupported }
            .filter { it.isInteropEnabled }

    private val FunctionDescriptor.isSupported: Boolean
        get() = this.isSuspend

    private val FunctionDescriptor.isInteropEnabled: Boolean
        get() = this.getConfiguration(SuspendInterop.Enabled)

    private fun markOriginalFunctionAsReplaced(originalFunctionDescriptor: SimpleFunctionDescriptor) {
        module.configure {
            originalFunctionDescriptor.swiftModel.visibility = SwiftModelVisibility.Replaced
        }
    }
}
