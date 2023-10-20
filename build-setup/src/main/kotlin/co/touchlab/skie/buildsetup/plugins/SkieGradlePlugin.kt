package co.touchlab.skie.buildsetup.plugins

import com.gradle.publish.PublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<SkieBase>()
        apply<KotlinPluginWrapper>()
        apply<DevGradleImplicitReceiver>()
        apply<PublishPlugin>()
    }
}
