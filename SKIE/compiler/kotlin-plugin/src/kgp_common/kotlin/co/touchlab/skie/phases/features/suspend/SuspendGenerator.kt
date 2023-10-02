package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.SuspendInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.kir.allExposedMembers
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.phases.util.doInPhase
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

object SuspendGenerator : DescriptorModificationPhase {

    context(DescriptorModificationPhase.Context)
    override fun isActive(): Boolean = isEnabled()

    private fun SkiePhase.Context.isEnabled(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(DescriptorModificationPhase.Context)
    override fun execute() {
        val kotlinDelegate = KotlinSuspendGeneratorDelegate(context)
        val swiftDelegate = SwiftSuspendGeneratorDelegate(context)

        descriptorProvider.allSupportedFunctions.forEach { function ->
            val kotlinBridgingFunction = kotlinDelegate.generateKotlinBridgingFunction(function)

            swiftDelegate.generateSwiftBridgingFunction(function, kotlinBridgingFunction)

            markOriginalFunctionAsReplaced(function)
        }
    }

    context(SkiePhase.Context)
    private val MutableDescriptorProvider.allSupportedFunctions: List<SimpleFunctionDescriptor>
        get() = this.allExposedMembers.filterIsInstance<SimpleFunctionDescriptor>()
            .filter { this.isBaseMethod(it) }
            .filter { it.isSupported }
            .filter { it.isInteropEnabled }

    context(SkiePhase.Context)
    fun hasSuspendWrapper(swiftModel: KotlinFunctionSwiftModel): Boolean =
        isEnabled() && swiftModel.descriptor.isSupported && swiftModel.descriptor.isInteropEnabled

    private val FunctionDescriptor.isSupported: Boolean
        get() = this.isSuspend

    context(SkiePhase.Context)
    private val FunctionDescriptor.isInteropEnabled: Boolean
        get() = this.getConfiguration(SuspendInterop.Enabled)

    context(SkiePhase.Context)
    private fun markOriginalFunctionAsReplaced(originalFunctionDescriptor: SimpleFunctionDescriptor) {
        context.doInPhase(KotlinBridgeConfigurationPhase) {
            originalFunctionDescriptor.swiftModel.visibility = SwiftModelVisibility.Replaced
        }
    }

    object KotlinBridgeConfigurationPhase : StatefulSirPhase()

    object SwiftBridgeGeneratorPhase : StatefulSirPhase()
}
