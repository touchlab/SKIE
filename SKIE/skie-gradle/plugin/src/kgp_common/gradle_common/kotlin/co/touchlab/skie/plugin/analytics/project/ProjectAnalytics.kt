package co.touchlab.skie.plugin.analytics.project

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import co.touchlab.skie.util.hash.hashed
import org.gradle.api.Project

internal data class ProjectAnalytics(
    val projectId: String,
) {

    class Producer(private val project: Project) : AnalyticsProducer {

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Project

        override val name: String = "project"

        override fun produce(): String =
            ProjectAnalytics(
                projectId = "${project.rootProject.name}${project.path}${project.group}".hashed(),
            ).toPrettyJson()
    }
}
