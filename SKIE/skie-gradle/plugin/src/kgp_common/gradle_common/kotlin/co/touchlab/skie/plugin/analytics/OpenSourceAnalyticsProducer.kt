package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.configuration.SkieFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsProducer
import groovy.json.JsonOutput
import org.gradle.api.Project

internal class OpenSourceAnalyticsProducer(private val project: Project) : AnalyticsProducer {

    override val feature: SkieFeature = SkieFeature.Analytics_OpenSource

    override val name: String = "open-source"

    override fun produce(): String =
        OpenSourceAnalytics(
            rootProjectName = project.rootProject.name,
            projectFullName = "${project.rootProject.name}${project.path}",
        ).serialize()
}

private fun OpenSourceAnalytics.serialize(): String =
    JsonOutput.toJson(this)
