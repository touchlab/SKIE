package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirOverridableDeclaration
import co.touchlab.skie.kir.util.getEntireOverrideHierarchy
import co.touchlab.skie.phases.SirPhase

class UnifyFlowConfigurationForOverridesPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val kirProvider = context.kirProvider

    context(SirPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(SirPhase.Context)
    override suspend fun execute() {
        kirProvider.allOverridableDeclaration
            .filter { it.overriddenDeclarations.isEmpty() && it.overriddenBy.isNotEmpty() }
            .forEach {
                it.unifyConfigurationForOverrides()
            }
    }

    private fun KirOverridableDeclaration<*, *>.unifyConfigurationForOverrides() {
        @Suppress("UNCHECKED_CAST")
        val allOverrides = this.getEntireOverrideHierarchy() as List<KirOverridableDeclaration<*, *>>

        val isFlowInteropEnabled = allOverrides.any { it.configuration.flowMappingStrategy == FlowMappingStrategy.Full }

        val newFlowMappingStrategy = if (isFlowInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None

        allOverrides.forEach {
            it.setFlowMappingStrategy(newFlowMappingStrategy)
        }
    }

    private fun KirCallableDeclaration<*>.setFlowMappingStrategy(
        newFlowMappingStrategy: FlowMappingStrategy,
    ) {
        this.configuration.flowMappingStrategy = newFlowMappingStrategy

        if (this is KirFunction<*>) {
            this.valueParameters.forEach { valueParameter ->
                valueParameter.configuration.flowMappingStrategy = newFlowMappingStrategy
            }
        }
    }
}
