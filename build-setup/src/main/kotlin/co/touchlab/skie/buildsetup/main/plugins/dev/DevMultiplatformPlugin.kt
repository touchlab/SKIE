package co.touchlab.skie.buildsetup.main.plugins.dev

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

// WIP
abstract class DevMultiplatformPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlinPlugin>()
        apply<KotlinMultiplatformPluginWrapper>()
    }
}
