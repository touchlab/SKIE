package co.touchlab.skie.buildsetup.gradle.plugins.gradle

import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiver
import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupport
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.gradlePluginApi
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class GradleShim : org.gradle.api.Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<BaseKotlin>()
        apply<UtilityGradleMinimumTargetKotlinVersion>()
        apply<UtilityGradleImplicitReceiver>()
        apply<UtilityMultiKotlinVersionSupport>()
        apply<KotlinPluginWrapper>()

        configureDependencies()
    }

    private fun Project.configureDependencies() {
        val primaryKotlinVersion = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).primaryVersion

        dependencies {
            compileOnly(gradlePluginApi())
            compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$primaryKotlinVersion")
            compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$primaryKotlinVersion")
        }
    }
}
