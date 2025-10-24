package co.touchlab.skie.buildsetup.gradle.plugins.gradle

import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiverPlugin
import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupportPlugin
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.gradlePluginApi
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class GradleShimPlugin : org.gradle.api.Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<BaseKotlinPlugin>()
        apply<UtilityGradleMinimumTargetKotlinVersionPlugin>()
        apply<UtilityGradleImplicitReceiverPlugin>()
        apply<UtilityMultiKotlinVersionSupportPlugin>()
        apply<KotlinPluginWrapper>()

        configureDependencies()
    }

    private fun Project.configureDependencies() {
        val primaryCompilerVersion = SupportedKotlinVersionProvider.getPrimaryKotlinVersion(project).compilerVersion

        dependencies {
            compileOnly(gradlePluginApi())
            compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$primaryCompilerVersion")
            compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$primaryCompilerVersion")
        }
    }
}
