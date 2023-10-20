package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class SkieRuntimeSwift : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<KotlinPluginWrapper>()
    }
}
