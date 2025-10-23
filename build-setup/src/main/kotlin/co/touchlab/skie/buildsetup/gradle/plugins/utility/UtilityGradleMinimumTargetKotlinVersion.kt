@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.gradle.plugins.utility

import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersion
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.minGradleVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

abstract class UtilityGradleMinimumTargetKotlinVersion : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val minimumVersion = KotlinToolingVersion(minGradleVersion().embeddedKotlin)

        UtilityMinimumTargetKotlinVersion.setMinimumTargetKotlinVersion(project, minimumVersion)
    }
}
