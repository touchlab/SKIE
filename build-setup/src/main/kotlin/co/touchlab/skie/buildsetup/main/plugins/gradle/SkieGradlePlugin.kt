package co.touchlab.skie.buildsetup.main.plugins.gradle

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityGradleImplicitReceiver
import com.gradle.publish.PublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlin>()
        apply<KotlinPluginWrapper>()
        apply<UtilityGradleImplicitReceiver>()
        apply<PublishPlugin>()
    }
}
