package co.touchlab.skie.configuration.provider

import co.touchlab.skie.configuration.ClassConfiguration
import co.touchlab.skie.configuration.ClassInterop
import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConstructorConfiguration
import co.touchlab.skie.configuration.DefaultArgumentInterop
import co.touchlab.skie.configuration.EnumInterop
import co.touchlab.skie.configuration.ExperimentalFeatures
import co.touchlab.skie.configuration.FileConfiguration
import co.touchlab.skie.configuration.FileOrClassConfiguration
import co.touchlab.skie.configuration.FlowInterop
import co.touchlab.skie.configuration.FunctionInterop
import co.touchlab.skie.configuration.GlobalConfiguration
import co.touchlab.skie.configuration.ModuleConfiguration
import co.touchlab.skie.configuration.PackageConfiguration
import co.touchlab.skie.configuration.PropertyConfiguration
import co.touchlab.skie.configuration.SealedInterop
import co.touchlab.skie.configuration.SimpleFunctionConfiguration
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.configuration.SkieVisibility
import co.touchlab.skie.configuration.SuppressSkieWarning
import co.touchlab.skie.configuration.SuspendInterop
import co.touchlab.skie.configuration.ValueParameterConfiguration
import co.touchlab.skie.configuration.ValueParameterConfigurationParent
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData.Group
import co.touchlab.skie.util.Optional
import co.touchlab.skie.util.toOptional

class ConfigurationProvider(
    private val configurationData: CompilerSkieConfigurationData,
    pluginConfigurationKeys: Set<ConfigurationKey<*>>,
) {

    private val builtInKeys = setOf<ConfigurationKey<*>>(
        ClassInterop.StableTypeAlias,
        ClassInterop.CInteropFrameworkName,
        ClassInterop.DeriveCInteropFrameworkNameFromCocoapods,
        DefaultArgumentInterop.Enabled,
        DefaultArgumentInterop.MaximumDefaultArgumentCount,
        EnumInterop.Enabled,
        EnumInterop.LegacyCaseName,
        ExperimentalFeatures.Enabled,
        FlowInterop.Enabled,
        FunctionInterop.FileScopeConversion.Enabled,
        FunctionInterop.LegacyName,
        SealedInterop.Enabled,
        SealedInterop.ExportEntireHierarchy,
        SealedInterop.Function.Name,
        SealedInterop.Function.ArgumentLabel,
        SealedInterop.Function.ParameterName,
        SealedInterop.ElseName,
        SealedInterop.Case.Visible,
        SealedInterop.Case.Name,
        SkieVisibility,
        SuppressSkieWarning.NameCollision,
        SuspendInterop.Enabled,
    )

    private val allKeys = builtInKeys + pluginConfigurationKeys

    val globalConfiguration: GlobalConfiguration by lazy {
        GlobalConfiguration(configurationData.enabledConfigurationFlags, allKeys).withLoadedKeyValueConfiguration(IdentifiedConfigurationTarget.Root)
    }

    private val cache = mutableMapOf<IdentifiedConfigurationTarget, SkieConfiguration>()

    fun getConfiguration(target: IdentifiedConfigurationTarget.Module): ModuleConfiguration =
        cache.getOrPut(target) {
            ModuleConfiguration(globalConfiguration).withLoadedKeyValueConfiguration(target)
        } as ModuleConfiguration

    fun getConfiguration(target: IdentifiedConfigurationTarget.Package): PackageConfiguration =
        cache.getOrPut(target) {
            val parent = getConfiguration(target.parent)

            PackageConfiguration(parent).withLoadedKeyValueConfiguration(target)
        } as PackageConfiguration

    fun getConfiguration(target: IdentifiedConfigurationTarget.File): FileConfiguration =
        cache.getOrPut(target) {
            val parent = getConfiguration(target.parent)

            FileConfiguration(parent).withLoadedKeyValueConfiguration(target)
        } as FileConfiguration

    fun getConfiguration(target: IdentifiedConfigurationTarget.Class): ClassConfiguration =
        cache.getOrPut(target) {
            val parent = getFileOrClassConfiguration(target.parent)

            ClassConfiguration(parent).withLoadedKeyValueConfiguration(target)
        } as ClassConfiguration

    private fun getFileOrClassConfiguration(target: IdentifiedConfigurationTarget.FileOrClass): FileOrClassConfiguration =
        when (target) {
            is IdentifiedConfigurationTarget.File -> FileOrClassConfiguration.File(getConfiguration(target))
            is IdentifiedConfigurationTarget.Class -> FileOrClassConfiguration.Class(getConfiguration(target))
        }

    private fun getValueParameterParentConfiguration(
        target: IdentifiedConfigurationTarget.ValueParameterParent,
    ): ValueParameterConfigurationParent =
        when (target) {
            is IdentifiedConfigurationTarget.Constructor -> ValueParameterConfigurationParent.CallableDeclaration(getConfiguration(target))
            is IdentifiedConfigurationTarget.SimpleFunction -> ValueParameterConfigurationParent.CallableDeclaration(getConfiguration(target))
            is IdentifiedConfigurationTarget.Property -> ValueParameterConfigurationParent.CallableDeclaration(getConfiguration(target))
            is IdentifiedConfigurationTarget.Class -> ValueParameterConfigurationParent.Class(getConfiguration(target))
        }

    fun getConfiguration(target: IdentifiedConfigurationTarget.Constructor): ConstructorConfiguration =
        cache.getOrPut(target) {
            val parent = getFileOrClassConfiguration(target.parent)

            ConstructorConfiguration(parent).withLoadedKeyValueConfiguration(target)
        } as ConstructorConfiguration

    fun getConfiguration(target: IdentifiedConfigurationTarget.SimpleFunction): SimpleFunctionConfiguration =
        cache.getOrPut(target) {
            val parent = getFileOrClassConfiguration(target.parent)

            SimpleFunctionConfiguration(parent).withLoadedKeyValueConfiguration(target)
        } as SimpleFunctionConfiguration

    fun getConfiguration(target: IdentifiedConfigurationTarget.Property): PropertyConfiguration =
        cache.getOrPut(target) {
            val parent = getFileOrClassConfiguration(target.parent)

            PropertyConfiguration(parent).withLoadedKeyValueConfiguration(target)
        } as PropertyConfiguration

    fun getConfiguration(target: IdentifiedConfigurationTarget.ValueParameter): ValueParameterConfiguration =
        cache.getOrPut(target) {
            val parent = getValueParameterParentConfiguration(target.parent)

            ValueParameterConfiguration(parent).withLoadedKeyValueConfiguration(target)
        } as ValueParameterConfiguration

    private fun <T : SkieConfiguration> T.withLoadedKeyValueConfiguration(target: IdentifiedConfigurationTarget): T {
        if (target.belongsToSkieRuntime) {
            useDefaultsForSkieRuntime = true
        } else {
            configureValuesForKeys(target)
        }

        return this
    }

    private fun <T : SkieConfiguration> T.configureValuesForKeys(target: IdentifiedConfigurationTarget) {
        val applicableKeys = allKeys.filter { target.scopeType.isInstance(it) }

        applicableKeys.forEach { key ->
            configureValueForKey(target, key)
        }
    }

    private fun <T> SkieConfiguration.configureValueForKey(target: IdentifiedConfigurationTarget, key: ConfigurationKey<T>) {
        key.findValue(target).ifSome {
            setUnsafe(key, it)
        }
    }

    private fun <T> ConfigurationKey<T>.findValue(target: IdentifiedConfigurationTarget): Optional<T> {
        val group = findGroup(target)

        return when {
            group?.overridesAnnotations == true -> getValue(group).toOptional()
            this.hasAnnotationValue(target) -> getAnnotationValue(target).toOptional()
            group != null -> getValue(group).toOptional()
            else -> Optional.None
        }
    }

    private fun ConfigurationKey<*>.findGroup(target: IdentifiedConfigurationTarget): Group? =
        configurationData.groups.lastOrNull { target.fqName.startsWith(it.target) && this.name in it.items }

    private fun <T> ConfigurationKey<T>.getValue(group: Group): T =
        group.items.getValue(this.name).let { this.deserialize(it) }
}
