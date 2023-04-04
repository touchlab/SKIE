package co.touchlab.skie.plugin.generator.internal.coroutines.flow

import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.configuration.gradle.FlowInterop
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

internal class FlowMappingConfigurator(
    override val skieContext: SkieContext,
) : SkieCompilationPhase, ConfigurationContainer {

    override val isActive: Boolean = SkieFeature.CoroutinesInterop in configuration.enabledFeatures

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
