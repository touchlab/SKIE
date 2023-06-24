package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.version.darwinPlatform
import co.touchlab.skie.gradle.version.darwinPlatformDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

abstract class SkieRuntimeKotlin: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(darwinPlatformDimension(), kotlinToolingVersionDimension()) { target ->
                val preset = presets.getByName(target.darwinPlatform.name)
                targetFromPreset(preset, target.name) {
                    this.attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                    }

                    // These two configurations are created by Kotlin, but don't copy our attributes, so we need to do it manually
                    configurations.named(target.name + "CInteropApiElements").configure {
                        this.attributes {
                            attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                        }
                    }

                    configurations.named(target.name + "MetadataElements").configure {
                        this.attributes {
                            attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                        }
                    }
                }
            }

            configureSourceSet { sourceSet ->
                configureRelatedConfigurations {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(sourceSet.kotlinToolingVersion.value))
                    }
                }
            }
        }
    }
}
