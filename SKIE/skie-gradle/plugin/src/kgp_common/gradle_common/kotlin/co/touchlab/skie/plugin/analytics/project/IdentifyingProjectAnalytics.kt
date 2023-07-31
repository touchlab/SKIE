package co.touchlab.skie.plugin.analytics.project

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import org.gradle.api.Project

internal data class IdentifyingProjectAnalytics(
    val rootProjectName: String,
    val rootProjectPath: String,
    val projectFullName: String,
) {

    class Producer(private val project: Project) : AnalyticsProducer {

        override val feature: SkieFeature = SkieFeature.Analytics_Identifying_Project

        override val name: String = "identifying-project"

        override fun produce(): String =
            project.getIdentifyingProjectAnalytics()
                .toPrettyJson()
    }
}

internal fun Project.getIdentifyingProjectAnalytics(): IdentifyingProjectAnalytics =
    IdentifyingProjectAnalytics(
        rootProjectName = project.rootProject.name,
        projectFullName = "${project.rootProject.name}${project.path}",
        rootProjectPath = project.rootDir.absolutePath,
    )
