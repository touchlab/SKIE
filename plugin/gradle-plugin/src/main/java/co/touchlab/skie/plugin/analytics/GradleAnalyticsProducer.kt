package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.gradle.GradleAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.plugin.license.SkieLicense
import co.touchlab.skie.util.hashed
import co.touchlab.skie.util.redacted
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import kotlin.reflect.KClass

internal class GradleAnalyticsProducer(
    private val project: Project,
    private val license: SkieLicense,
) : AnalyticsProducer<AnalyticsFeature.Gradle> {

    override val featureType: KClass<AnalyticsFeature.Gradle> = AnalyticsFeature.Gradle::class

    override val name: String = "gradle"

    override fun produce(configuration: AnalyticsFeature.Gradle): ByteArray =
        getGradleAnalytics(configuration).encode()

    private fun getGradleAnalytics(configuration: AnalyticsFeature.Gradle): GradleAnalytics =
        if (configuration.stripIdentifiers) {
            getFullGradleAnalytics().redacted()
        } else {
            getFullGradleAnalytics()
        }

    private fun getFullGradleAnalytics(): GradleAnalytics = GradleAnalytics(
        rootProjectName = project.rootProject.name,
        projectPath = project.path,
        projectFullNameHash = "${project.rootProject.name}${project.path}".hashed(),
        rootProjectDiskLocationHash = project.rootProject.projectDir.absolutePath.hashed(),
        organizationKey = license.organizationKey,
        licenseKey = license.licenseKey,
        skieVersion = BuildConfig.KOTLIN_PLUGIN_VERSION,
        gradleVersion = project.gradle.gradleVersion,
        kotlinVersion = project.getKotlinPluginVersion(),
        stdlibVersion = project.kotlinExtension.coreLibrariesVersion,
        timestampInMs = System.currentTimeMillis(),
        isCI = isCI(),
    )

    private fun isCI(): Boolean =
        !System.getenv("CI").isNullOrBlank()
}

private fun GradleAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()

private fun GradleAnalytics.redacted(): GradleAnalytics =
    copy(
        rootProjectName = rootProjectName.redacted(),
        projectPath = projectPath.redacted(),
    )
