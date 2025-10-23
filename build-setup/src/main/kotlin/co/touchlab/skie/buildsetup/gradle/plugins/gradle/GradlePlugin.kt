package co.touchlab.skie.buildsetup.gradle.plugins.gradle

import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiver
import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import com.gradle.publish.PublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class GradlePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlin>()
        apply<UtilityGradleMinimumTargetKotlinVersion>()
        apply<UtilityGradleImplicitReceiver>()
        apply<KotlinPluginWrapper>()
        apply<PublishPlugin>()
    }
}
