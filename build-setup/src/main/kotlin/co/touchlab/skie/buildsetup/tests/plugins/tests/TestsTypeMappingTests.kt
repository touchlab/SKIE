@file:OptIn(ExternalKotlinTargetApi::class, ExperimentalKotlinGradlePluginApi::class)

package co.touchlab.skie.buildsetup.tests.plugins.tests

import co.touchlab.skie.buildsetup.tests.plugins.base.BaseTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExternalKotlinTargetApi

abstract class TestsTypeMappingTests : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<BaseTests>()
    }
}
