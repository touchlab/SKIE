package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.util.testImplementation
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class DevJvm: Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(kotlinToolingVersionDimension()) { target ->
                jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                    }
                }
            }

            configureSourceSet { sourceSet ->
                val kotlinVersion = sourceSet.kotlinToolingVersion.value

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    weak("org.jetbrains.kotlin:kotlin-native-compiler-embeddable:$kotlinVersion")

                    testOnly(libs.bundles.testing.jvm)
                }
            }
        }
    }
}
