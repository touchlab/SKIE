package co.touchlab.skie.buildsetup.main.plugins.dev

import co.touchlab.skie.buildsetup.main.plugins.base.BaseRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

abstract class DevRoot : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseRoot>()
    }
}
