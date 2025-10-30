package co.touchlab.skie.buildsetup.main.plugins.utility

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper

abstract class UtilityOptInCompilerApiPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val annotations = listOf(
            "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
            "org.jetbrains.kotlin.backend.konan.InternalKotlinNativeApi",
            "org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI",
            "org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI",
            "org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI",
        )

        plugins.withType<KotlinBasePluginWrapper>().configureEach {
            extensions.configure<KotlinBaseExtension> {
                sourceSets.configureEach {
                    languageSettings {
                        annotations.forEach {
                            optIn(it)
                        }
                    }
                }
            }
        }
    }
}
