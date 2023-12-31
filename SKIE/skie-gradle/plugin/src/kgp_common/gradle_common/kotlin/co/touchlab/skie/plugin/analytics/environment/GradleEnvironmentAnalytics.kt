package co.touchlab.skie.plugin.analytics.environment

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import co.touchlab.skie.util.BuildConfig
import io.cloudflight.ci.info.CI
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

internal data class GradleEnvironmentAnalytics(
    val jvmVersion: String,
    val skieVersion: String,
    val gradleVersion: String,
    val kotlinVersion: String,
    val macOSVersion: String,
    val ci: String?,
    val timestampInMs: Long,
) {

    class Producer(private val project: Project) : AnalyticsProducer {

        override val name: String = "gradle-environment"

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_GradleEnvironment

        override fun produce(): String =
            GradleEnvironmentAnalytics(
                jvmVersion = Runtime.version().toString(),
                skieVersion = BuildConfig.SKIE_VERSION,
                gradleVersion = project.gradle.gradleVersion,
                kotlinVersion = project.getKotlinPluginVersion(),
                macOSVersion = System.getProperty("os.version"),
                ci = CI.server?.serverName,
                timestampInMs = System.currentTimeMillis(),
            ).toPrettyJson()
    }
}
