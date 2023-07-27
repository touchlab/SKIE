package co.touchlab.skie.buildsetup.plugins.extensions

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

interface HasMavenPublishPlugin {
    val Project.publishing: PublishingExtension
        get() = extensions.getByType()

    fun Project.publishing(configure: PublishingExtension.() -> Unit) = extensions.configure(configure)
}
