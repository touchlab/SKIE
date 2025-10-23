package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

abstract class SkieRuntimeKotlin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlin>()
        apply<UtilityMinimumTargetKotlinVersion>()
        apply<KotlinMultiplatformPluginWrapper>()
    }
}
