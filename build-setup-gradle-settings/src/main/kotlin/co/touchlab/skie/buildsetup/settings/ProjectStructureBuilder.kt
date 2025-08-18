package co.touchlab.skie.buildsetup.settings

import org.gradle.api.initialization.Settings

inline fun Settings.projectStructure(configure: ProjectStructureBuilderScope.() -> Unit) {
    configure(ProjectStructureBuilderScope(this, emptyList(), null))
}
