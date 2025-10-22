package co.touchlab.skie.buildsetup.main.plugins.utility

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigPlugin
import com.github.gmazzo.gradle.plugins.generators.BuildConfigKotlinGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

abstract class UtilityBuildConfig : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BuildConfigPlugin>()

        extensions.configure<BuildConfigExtension> {
            packageName(("${project.group}.${project.name}").replace("-", "_"))

            generator.set(BuildConfigKotlinGenerator())
        }
    }
}
