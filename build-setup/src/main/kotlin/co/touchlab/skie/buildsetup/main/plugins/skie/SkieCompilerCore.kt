package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApi
import co.touchlab.skie.buildsetup.version.KotlinCompilerVersionEnumGenerator
import co.touchlab.skie.gradle.util.compileOnly
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class SkieCompilerCore : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlin>()
        apply<UtilityMinimumTargetKotlinVersion>()
        apply<UtilityOptInExperimentalCompilerApi>()
        apply<KotlinPluginWrapper>()

        KotlinCompilerVersionEnumGenerator.generate(project, "co.touchlab.skie", true)

        project.dependencies {
            compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
        }
    }
}
