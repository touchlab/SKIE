package co.touchlab.skie.buildsetup.settings

import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ProjectStructureBuilderScope(
    private val settings: Settings,
    private val path: List<String>,
    private val prefix: String?,
) {

    val module: ModuleDelegateProvider
        get() = ModuleDelegateProvider(isGroup = false, directoryNameOverride = null) { }

    fun module(directoryNameOverride: String? = null, configure: ProjectStructureBuilderScope.() -> Unit = { }): ModuleDelegateProvider {
        return ModuleDelegateProvider(isGroup = false, directoryNameOverride = directoryNameOverride, configure = configure)
    }

    fun group(directoryNameOverride: String? = null, configure: ProjectStructureBuilderScope.() -> Unit = { }): ModuleDelegateProvider {
        return ModuleDelegateProvider(isGroup = true, directoryNameOverride = directoryNameOverride, configure = configure)
    }

    private fun module(name: String, provider: ModuleDelegateProvider): ProjectDescriptor {
        val prefixedName = listOfNotNull(prefix, name).joinToString("-")
        val modulePath = path + name
        val modulePathString = modulePath.joinToString(":")
        settings.include(modulePathString)
        return settings.project(":$modulePathString").also {
            it.name = prefixedName
            it.buildFileName = "$prefixedName.gradle.kts"
            if (provider.directoryNameOverride != null) {
                it.projectDir = it.projectDir.parentFile.resolve(provider.directoryNameOverride)
            }
            provider.configure(ProjectStructureBuilderScope(settings, modulePath, prefixedName.takeIf { provider.isGroup }))
        }
    }

    class ValueProperty<T>(private val value: T) : ReadOnlyProperty<Nothing?, T> {

        override fun getValue(thisRef: Nothing?, property: KProperty<*>): T = value
    }

    inner class ModuleDelegateProvider(
        internal val isGroup: Boolean,
        internal val directoryNameOverride: String?,
        internal val configure: ProjectStructureBuilderScope.() -> Unit,
    ) : PropertyDelegateProvider<Nothing?, ValueProperty<ProjectDescriptor>> {

        override fun provideDelegate(thisRef: Nothing?, property: KProperty<*>): ValueProperty<ProjectDescriptor> {
            return ValueProperty(module(property.name, this))
        }
    }
}
