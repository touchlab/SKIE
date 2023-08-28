package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.util.named
import co.touchlab.skie.plugin.util.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal fun KotlinNativeTarget.addDependencyOnSkieRuntime() {
    if (!project.isCoroutinesInteropEnabled) {
        return
    }

    project.configurations.configureEach {
        attributes {
            attribute(KotlinCompilerVersion.attribute, project.objects.named(BuildConfig.KOTLIN_TOOLING_VERSION))
        }
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
