package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.isSuspendInteropEnabled
import co.touchlab.skie.configuration.provider.descriptor.configuration
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.descriptor.allExposedMembers
import co.touchlab.skie.phases.FrontendIrPhase
import co.touchlab.skie.phases.descriptorProvider
import co.touchlab.skie.phases.util.StatefulKirPhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import co.touchlab.skie.util.isComposable
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

object SuspendGenerator : FrontendIrPhase {

    context(FrontendIrPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(FrontendIrPhase.Context)
    override suspend fun execute() {
        val kotlinDelegate = KotlinSuspendGeneratorDelegate(context)
        val swiftDelegate = SwiftSuspendGeneratorDelegate(context)

        descriptorProvider.allSupportedFunctions.forEach { function ->
            val kotlinBridgingFunction = kotlinDelegate.generateKotlinBridgingFunction(function)

            swiftDelegate.generateSwiftBridgingFunction(function, kotlinBridgingFunction)
        }
    }

    context(FrontendIrPhase.Context)
    private val DescriptorProvider.allSupportedFunctions: List<SimpleFunctionDescriptor>
        get() = this.allExposedMembers.filterIsInstance<SimpleFunctionDescriptor>()
            .filter { mapper.isBaseMethod(it) }
            .filter { it.isSupported }
            .filter { it.configuration.isSuspendInteropEnabled }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.isSuspend && !this.isComposable

    object FlowMappingConfigurationPhase : StatefulKirPhase()

    object KotlinBridgingFunctionVisibilityConfigurationInitPhase : StatefulKirPhase()

    object KotlinBridgingFunctionVisibilityConfigurationFinalizePhase : StatefulSirPhase()

    object SwiftBridgeGeneratorInitPhase : StatefulKirPhase()

    object SwiftBridgeGeneratorFinalizePhase : StatefulSirPhase()
}
