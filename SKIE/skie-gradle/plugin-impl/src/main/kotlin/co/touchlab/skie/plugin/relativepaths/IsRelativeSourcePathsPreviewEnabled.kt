package co.touchlab.skie.plugin.relativepaths

import co.touchlab.skie.plugin.configuration.skieExtension
import org.gradle.api.Project

val Project.isRelativeSourcePathsPreviewEnabled: Boolean
    get() = project.skieExtension.build.enableRelativeSourcePathsInDebugSymbols.get()
