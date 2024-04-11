package co.touchlab.skie.phases.features.suspend

import co.touchlab.skie.configuration.SimpleFunctionConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.SuspendInterop
import co.touchlab.skie.configuration.isSuspendInteropEnabled
import co.touchlab.skie.configuration.provider.descriptor.configuration
import co.touchlab.skie.kir.descriptor.DescriptorProvider
import co.touchlab.skie.kir.descriptor.allExposedMembers
import co.touchlab.skie.phases.DescriptorModificationPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.util.StatefulSirPhase
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor

object SuspendGenerator : DescriptorModificationPhase {

    context(DescriptorModificationPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(DescriptorModificationPhase.Context)
    override suspend fun execute() {
        val kotlinDelegate = KotlinSuspendGeneratorDelegate(context)
        val swiftDelegate = SwiftSuspendGeneratorDelegate(context)

        descriptorProvider.allSupportedFunctions.forEach { function ->
            val kotlinBridgingFunction = kotlinDelegate.generateKotlinBridgingFunction(function)

            swiftDelegate.generateSwiftBridgingFunction(function, kotlinBridgingFunction)
        }
    }

    context(SkiePhase.Context)
    private val DescriptorProvider.allSupportedFunctions: List<SimpleFunctionDescriptor>
        get() = this.allExposedMembers.filterIsInstance<SimpleFunctionDescriptor>()
            .filter { mapper.isBaseMethod(it) }
            .filter { it.isSupported }
            .filter { it.configuration.isSuspendInteropEnabled }

    private val SimpleFunctionDescriptor.isSupported: Boolean
        get() = this.isSuspend

    object FlowMappingConfigurationPhase : StatefulSirPhase()

    object KotlinBridgingFunctionVisibilityConfigurationPhase : StatefulSirPhase()

    object SwiftBridgeGeneratorPhase : StatefulSirPhase()
}
