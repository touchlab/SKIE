package co.touchlab.skie.gradle.buildconfig

import com.github.gmazzo.gradle.plugins.BuildConfigExtension
import com.github.gmazzo.gradle.plugins.BuildConfigPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class SkieBuildConfigPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        applyBuildConfigPlugin(target)
        configureBuildConfigPackageName(target)
    }

    private fun applyBuildConfigPlugin(target: Project) {
        target.plugins.apply(BuildConfigPlugin::class.java)
    }

    private fun configureBuildConfigPackageName(target: Project) {
        target.extensions.configure(BuildConfigExtension::class.java) {
            packageName(("${target.group}.${target.name}").replace("-", "_"))
        }
    }
}
