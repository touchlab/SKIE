package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.KotlinToolingVersion
import co.touchlab.skie.gradle.version.DarwinPlatformComponent
import co.touchlab.skie.gradle.version.darwinPlatform
import co.touchlab.skie.gradle.version.darwinPlatformDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import co.touchlab.skie.gradle.version.target.Target
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

abstract class SkieRuntimeKotlin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(darwinPlatformDimension(), kotlinToolingVersionDimension(), filter = { it.isSupported }) { target ->
                configureTarget(target)
            }

            configureSourceSet { sourceSet ->
                configureRelatedConfigurations {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(sourceSet.kotlinToolingVersion.value))
                    }
                }

                dependencies {
                    implementation("org.jetbrains.kotlin:kotlin-stdlib:${sourceSet.kotlinToolingVersion.primaryVersion}")
                }
            }
        }
    }

    private val Target.isSupported: Boolean
        // Runtime requires Coroutines but watchosDeviceArm64 is only supported since Coroutines 1.7.0 which require Kotlin 1.8.20
        get() = !(darwinPlatform == DarwinPlatformComponent.watchosDeviceArm64 && kotlinToolingVersion.primaryVersion == KotlinToolingVersion("1.8.0"))

    private fun KotlinMultiplatformExtension.configureTarget(target: Target): KotlinTarget {
        val preset = presets.getByName(target.darwinPlatform.name)

        return targetFromPreset(preset, target.name) {
            this.attributes {
                attribute(KotlinCompilerVersion.attribute, project.objects.named(target.kotlinToolingVersion.value))
            }

            // These two configurations are created by Kotlin, but don't copy our attributes, so we need to do it manually
            project.configurations.named(target.name + "CInteropApiElements").configure {
                this.attributes {
                    attribute(KotlinCompilerVersion.attribute, project.objects.named(target.kotlinToolingVersion.value))
                }
            }

            project.configurations.named(target.name + "MetadataElements").configure {
                this.attributes {
                    attribute(KotlinCompilerVersion.attribute, project.objects.named(target.kotlinToolingVersion.value))
                }
            }
        }
    }
}
