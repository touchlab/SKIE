package co.touchlab.skie.phases.features.flow

import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirOverridableDeclaration
import co.touchlab.skie.kir.element.classDescriptorOrNull
import co.touchlab.skie.kir.util.getEntireOverrideHierarchy
import co.touchlab.skie.phases.SirPhase
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

class ConfigureFlowConversionPhase(
    context: SirPhase.Context,
) : SirPhase {

    private val configurationProvider = context.configurationProvider
    private val kirProvider = context.kirProvider

    context(SirPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(SirPhase.Context)
    override fun execute() {
        copyFlowConfiguration()

        unifyConfigurationForOverrides()
    }

    // TODO This step will not be needed once all SKIE configuration is converted to Kir
    private fun copyFlowConfiguration() {
        kirProvider.allClasses.forEach {
            it.copyFlowConfiguration()
        }
    }

    private fun KirClass.copyFlowConfiguration() {
        this.classDescriptorOrNull?.flowMappingStrategy?.let {
            this.configuration.flowMappingStrategy = it
        }

        this.callableDeclarations.forEach {
            it.copyFlowConfiguration()
        }
    }

    private fun KirCallableDeclaration<*>.copyFlowConfiguration() {
        this.configuration.flowMappingStrategy = this.descriptor.flowMappingStrategy
    }

    val DeclarationDescriptor.flowMappingStrategy: FlowMappingStrategy
        get() = if (this.isFlowInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None

    private val DeclarationDescriptor.isFlowInteropEnabled: Boolean
        get() = configurationProvider.getConfiguration(this, FlowInterop.Enabled)

    private fun unifyConfigurationForOverrides() {
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

        allOverrides.forEach {
            it.configuration.flowMappingStrategy = if (isFlowInteropEnabled) FlowMappingStrategy.Full else FlowMappingStrategy.None
        }
    }
}
