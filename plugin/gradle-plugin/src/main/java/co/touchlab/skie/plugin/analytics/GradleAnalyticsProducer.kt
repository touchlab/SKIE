package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.gradle.GradleAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

internal class GradleAnalyticsProducer(
    private val project: Project,
) : AnalyticsProducer {

    override val name: String = "gradle"

    override fun produce(): ByteArray =
        GradleAnalytics(
            rootProjectName = project.rootProject.name,
            projectPath = project.path,
            // TODO
            licenseKey = "TODO",
            skieVersion = BuildConfig.KOTLIN_PLUGIN_VERSION,
            gradleVersion = project.gradle.gradleVersion,
            kotlinVersion = project.getKotlinPluginVersion(),
            stdlibVersion = project.kotlinExtension.coreLibrariesVersion,
            isCI = isCI(),
        ).encode()

    private fun isCI(): Boolean =
        !System.getenv("CI").isNullOrBlank()
}

private fun GradleAnalytics.encode(): ByteArray =
    Json.encodeToString(this).toByteArray()
