package co.touchlab.skie.buildsetup.gradle.plugins.gradle

import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiverPlugin
import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.gradlePluginApi
import com.gradle.publish.PublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class GradlePluginPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlinPlugin>()
        apply<UtilityGradleMinimumTargetKotlinVersionPlugin>()
        apply<UtilityGradleImplicitReceiverPlugin>()
        apply<KotlinPluginWrapper>()
        apply<PublishPlugin>()

        configureDependencies()
    }

    private fun Project.configureDependencies() {
        dependencies {
            compileOnly(gradlePluginApi())
        }
    }
}
