package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.util.compileOnly
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class SkieCommon : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlin>()
        apply<UtilityMinimumTargetKotlinVersion>()
        apply<KotlinPluginWrapper>()

        configureDependencies()
    }

    private fun Project.configureDependencies() {
        dependencies {
            compileOnly(kotlin("stdlib"))
        }
    }
}
