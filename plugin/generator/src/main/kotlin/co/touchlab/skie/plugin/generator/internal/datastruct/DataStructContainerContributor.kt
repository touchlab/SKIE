package co.touchlab.skie.plugin.generator.internal.datastruct

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.generator.ConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.platform.TargetPlatform

class DataStructContainerContributor(
    private val compilerConfiguration: CompilerConfiguration,
) : StorageComponentContainerContributor {

    override fun registerModuleComponents(
        container: StorageComponentContainer,
        platform: TargetPlatform,
        moduleDescriptor: ModuleDescriptor,
    ) {
        container.useInstance(DataStructDeclarationChecker(compilerConfiguration.pluginConfiguration))
    }

    private val CompilerConfiguration.pluginConfiguration: Configuration
        get() = get(ConfigurationKeys.swiftGenConfiguration, Configuration {})
}
