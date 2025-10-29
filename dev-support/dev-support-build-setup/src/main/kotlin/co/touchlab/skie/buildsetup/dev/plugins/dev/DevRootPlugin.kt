package co.touchlab.skie.buildsetup.dev.plugins.dev

import co.touchlab.skie.buildsetup.main.plugins.base.BaseRootPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

abstract class DevRootPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseRootPlugin>()
    }
}
