package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.plugin.configuration.skieExtension
import org.gradle.api.Project

val Project.isCoroutinesInteropEnabled: Boolean
    get() = project.skieExtension.features.coroutinesInterop.get()
