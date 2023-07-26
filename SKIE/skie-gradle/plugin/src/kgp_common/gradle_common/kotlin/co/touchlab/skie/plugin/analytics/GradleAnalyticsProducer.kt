package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import co.touchlab.skie.util.hashed
import groovy.json.JsonOutput
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

internal class GradleAnalyticsProducer(
    private val project: Project,
) : AnalyticsProducer {

    override val name: String = "gradle"

    override val feature: SkieFeature = SkieFeature.Analytics_Gradle

    override fun produce(): String =
        GradleAnalytics(
            //WIP
//             val buildId: String = UUID.randomUUID().toString()
            rootProjectName = project.rootProject.name,
            rootProjectNameHash = project.rootProject.name.hashed(),
            projectPath = project.path,
            projectFullName = "${project.rootProject.name}${project.path}",
            projectFullNameHash = "${project.rootProject.name}${project.path}".hashed(),
            rootProjectDiskLocationHash = rootProjectDiskLocationHash(project),
            organizationKey = "None",
            licenseKey = "None",
            skieVersion = BuildConfig.KOTLIN_PLUGIN_VERSION,
            gradleVersion = project.gradle.gradleVersion,
            kotlinVersion = project.getKotlinPluginVersion(),
            stdlibVersion = project.extensions.getByType(KotlinProjectExtension::class.java).coreLibrariesVersion,
            timestampInMs = System.currentTimeMillis(),
            isCI = isCI,
        ).serialize()

    companion object {

        val isCI: Boolean
            get() = !System.getenv("CI").isNullOrBlank()

        fun rootProjectDiskLocationHash(project: Project): String =
            project.rootProject.projectDir.absolutePath.hashed()
    }
}

private fun GradleAnalytics.serialize(): String =
    JsonOutput.toJson(this)
