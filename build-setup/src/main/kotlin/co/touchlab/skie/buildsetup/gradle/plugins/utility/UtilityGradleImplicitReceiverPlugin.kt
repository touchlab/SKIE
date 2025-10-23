package co.touchlab.skie.buildsetup.gradle.plugins.utility

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverGradleSubplugin

abstract class UtilityGradleImplicitReceiverPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply<SamWithReceiverGradleSubplugin>()

        project.extensions.configure<SamWithReceiverExtension> {
            annotation("org.gradle.api.HasImplicitReceiver")
        }
    }
}
