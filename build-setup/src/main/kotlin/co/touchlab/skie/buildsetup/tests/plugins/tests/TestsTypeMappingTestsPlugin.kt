package co.touchlab.skie.buildsetup.tests.plugins.tests

import co.touchlab.skie.buildsetup.tests.plugins.base.BaseTestsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

abstract class TestsTypeMappingTestsPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<BaseTestsPlugin>()
    }
}
