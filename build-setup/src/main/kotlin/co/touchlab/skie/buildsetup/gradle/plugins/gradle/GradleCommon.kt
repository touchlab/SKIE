package co.touchlab.skie.buildsetup.gradle.plugins.gradle

import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiver
import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.gradle.util.compileOnly
import co.touchlab.skie.gradle.version.minGradleVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class GradleCommon : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlin>()
        apply<UtilityGradleMinimumTargetKotlinVersion>()
        apply<UtilityGradleImplicitReceiver>()
        apply<KotlinPluginWrapper>()

        configureDependencies()
    }

    private fun Project.configureDependencies() {
        val minGradleVersion = minGradleVersion()

        dependencies {
            compileOnly("dev.gradleplugins:gradle-api:${minGradleVersion.gradle}")
            compileOnly("org.codehaus.groovy:groovy-json:${minGradleVersion.embeddedGroovy}")
        }
    }
}
