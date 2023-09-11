package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.configuration.ConfigurationContainer
import co.touchlab.skie.phases.SkieCompilationPhase

internal class FlowMappingConfigurator(
    override val skieContext: SkieContext,
) : SkieCompilationPhase, ConfigurationContainer {

    override val isActive: Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    private val callableMemberConfigurator = CallableMemberConfigurator()

    override fun runObjcPhase() {
        skieContext.module.configure {
            allExposedMembers.forEach {
                it.accept(callableMemberConfigurator)
            }
        }
    }

    private inner class CallableMemberConfigurator : MutableKotlinCallableMemberSwiftModelVisitor.Unit {

        override fun visit(function: MutableKotlinFunctionSwiftModel) {
            function.valueParameters.forEach {
                it.flowMappingStrategy = function.getTargetFlowMappingStrategy()
            }

            function.returnTypeFlowMappingStrategy = function.getTargetFlowMappingStrategy()
        }

        override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
            regularProperty.flowMappingStrategy = regularProperty.getTargetFlowMappingStrategy()
        }

        private fun KotlinCallableMemberSwiftModel.getTargetFlowMappingStrategy(): FlowMappingStrategy =
            if (this.isInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None

        private val KotlinCallableMemberSwiftModel.isInteropEnabled: Boolean
            get() = this.descriptor.getConfiguration(FlowInterop.Enabled)
    }
}
