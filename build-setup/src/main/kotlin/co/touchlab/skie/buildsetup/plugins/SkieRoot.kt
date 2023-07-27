package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

abstract class SkieRoot: Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        apply<DevRoot>()
        apply<SkieBase>()
    }
}
