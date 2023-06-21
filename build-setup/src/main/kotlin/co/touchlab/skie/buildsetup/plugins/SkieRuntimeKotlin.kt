package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.kotlin.apple
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.*
import co.touchlab.skie.gradle.version.DarwinPlatformComponent.*
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.TargetsFromPresetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import kotlin.io.path.name

class SkieRuntimeKotlin: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<KotlinMultiplatformPluginWrapper>()
        apply<MultiDimensionTargetPlugin>()

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(libs.versions.java)
        }

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(darwinPlatformDimension(), kotlinToolingVersionDimension())

            createTarget { target ->
                val preset = presets.getByName(target.darwinPlatform.name)
                targetFromPreset(preset, target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                    }
                }
            }

            configureSourceSet { sourceSet ->

            }
        }
    }
}
