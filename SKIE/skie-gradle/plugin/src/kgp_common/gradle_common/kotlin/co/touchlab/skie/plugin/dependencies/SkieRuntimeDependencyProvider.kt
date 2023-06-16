package co.touchlab.skie.plugin.dependencies

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.util.withType
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal fun KotlinNativeTarget.addDependencyOnSkieRuntime() {
    if (!project.isCoroutinesInteropEnabled) {
        return
    }

    compilations.named("main") {
        defaultSourceSet.dependencies {
            api(BuildConfig.RUNTIME_DEPENDENCY)
        }
    }

    binaries.withType<AbstractNativeLibrary>().configureEach {
        export(BuildConfig.RUNTIME_DEPENDENCY)
    }
}

// TODO Once we have new configuration this needs to depend on licensing
private val Project.isCoroutinesInteropEnabled: Boolean
    get() = project.skieExtension.features.coroutinesInterop.get()
