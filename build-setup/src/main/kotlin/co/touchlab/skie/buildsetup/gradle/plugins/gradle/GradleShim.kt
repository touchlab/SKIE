package co.touchlab.skie.buildsetup.gradle.plugins.gradle

import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiver
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMultiKotlinVersionSupport
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import co.touchlab.skie.gradle.util.compileOnly
import co.touchlab.skie.gradle.version.minGradleVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class GradleShim : org.gradle.api.Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<BaseKotlin>()
        // WIP Gradle variant targeting min supported Kotlin version
        apply<UtilityMinimumTargetKotlinVersion>()
        apply<UtilityGradleImplicitReceiver>()
        apply<UtilityMultiKotlinVersionSupport>()
        apply<KotlinPluginWrapper>()

        configureDependencies()
    }

    private fun Project.configureDependencies() {
        val minGradleVersion = minGradleVersion()

        val primaryKotlinVersion = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).primaryVersion

        dependencies {
            compileOnly("dev.gradleplugins:gradle-api:${minGradleVersion.gradle}")
            compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$primaryKotlinVersion")
            compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:$primaryKotlinVersion")
        }
    }
}
