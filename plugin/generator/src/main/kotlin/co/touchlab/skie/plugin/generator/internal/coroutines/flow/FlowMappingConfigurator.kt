package co.touchlab.skie.plugin.generator.internal.coroutines.flow

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.generator.internal.runtime.belongsToSkieRuntime
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase

internal class FlowMappingConfigurator(
    private val skieContext: SkieContext,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runObjcPhase() {
        skieContext.module.configure {
            allExposedMembers.forEach {
                it.accept(CallableMemberConfigurator)
            }
        }
    }

    private object CallableMemberConfigurator : MutableKotlinCallableMemberSwiftModelVisitor.Unit {

        override fun visit(function: MutableKotlinFunctionSwiftModel) {
            val strategy = if (function.descriptor.belongsToSkieRuntime) FlowMappingStrategy.None else FlowMappingStrategy.Full

            function.valueParameters.forEach {
                it.flowMappingStrategy = strategy
            }

            function.returnTypeFlowMappingStrategy = strategy
        }

        override fun visit(regularProperty: MutableKotlinRegularPropertySwiftModel) {
            val strategy = if (regularProperty.descriptor.belongsToSkieRuntime) FlowMappingStrategy.None else FlowMappingStrategy.Full

            regularProperty.flowMappingStrategy = strategy
        }
    }
}
