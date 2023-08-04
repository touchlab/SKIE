package co.touchlab.skie.plugin.analytics.project

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.analytics.AnalyticsProducer
import co.touchlab.skie.plugin.util.toPrettyJson
import co.touchlab.skie.util.hashed
import org.gradle.api.Project

internal data class AnonymousProjectAnalytics(
    val rootProjectNameHash: String,
    val rootProjectPathHash: String,
    val projectFullNameHash: String,
) {

    class Producer(private val project: Project) : AnalyticsProducer {

        override val configurationFlag: SkieConfigurationFlag = SkieConfigurationFlag.Analytics_Anonymous_Project

        override val name: String = "anonymous-project"

        override fun produce(): String =
            project.getIdentifyingProjectAnalytics()
                .let {
                    AnonymousProjectAnalytics(
                        rootProjectNameHash = it.rootProjectName.hashed(),
                        rootProjectPathHash = it.rootProjectPath.hashed(),
                        projectFullNameHash = it.projectFullName.hashed(),
                    )
                }
                .toPrettyJson()
    }
}
