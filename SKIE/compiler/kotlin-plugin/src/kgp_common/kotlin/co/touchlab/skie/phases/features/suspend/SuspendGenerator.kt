package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.SuspendInterop
import co.touchlab.skie.configuration.getConfiguration
import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.descriptor.allExposedMembers
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.util.StatefulSirPhase
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
        }
    }

    context(SkiePhase.Context)
    private val MutableDescriptorProvider.allSupportedFunctions: List<SimpleFunctionDescriptor>
        get() = this.allExposedMembers.filterIsInstance<SimpleFunctionDescriptor>()
            // WIP Remove and fix overrides that change return type but generate only for those that change the type
            .filter { this.isBaseMethod(it) }
            .filter { it.isSupported }
            .filter { it.isSuspendInteropEnabled }

    private val FunctionDescriptor.isSupported: Boolean
        get() = this.isSuspend

    object FlowMappingConfigurationPhase : StatefulSirPhase()

    object KotlinBridgingFunctionVisibilityConfigurationPhase : StatefulSirPhase()

    object SwiftBridgeGeneratorPhase : StatefulSirPhase()
}

// WIP Must be the same for the whole hierarchy

context(SkiePhase.Context)
val FunctionDescriptor.isSuspendInteropEnabled: Boolean
    get() = SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags &&
        this.getConfiguration(SuspendInterop.Enabled)

context(SkiePhase.Context)
val KirSimpleFunction.isSuspendInteropEnabled: Boolean
    get() = descriptor.isSuspendInteropEnabled
