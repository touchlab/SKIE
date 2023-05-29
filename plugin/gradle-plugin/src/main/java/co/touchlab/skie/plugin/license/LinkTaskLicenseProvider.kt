package co.touchlab.skie.plugin.license

import co.touchlab.skie.plugin.util.skieBuildDirectory
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal val KotlinNativeLink.license: Provider<SkieLicense>
    get() = project.provider { SkieLicenseProvider.loadLicense(skieBuildDirectory.license.toPath()) }
