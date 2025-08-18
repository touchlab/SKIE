package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityGradleImplicitReceiver
import co.touchlab.skie.gradle.KotlinCompilerVersionAttribute
import co.touchlab.skie.gradle.version.kotlinToolingVersion
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension
import co.touchlab.skie.gradle.version.minGradleVersion
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

abstract class SkieShim : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        apply<BaseKotlin>()
        apply<MultiDimensionTargetPlugin>()
        apply<UtilityGradleImplicitReceiver>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(kotlinToolingVersionDimension()) { target ->
                jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersionAttribute.attribute, objects.named(target.kotlinToolingVersion.value))
                    }
                }
            }

            configureSourceSet { sourceSet ->
                val kotlinToolingVersion = sourceSet.kotlinToolingVersion.primaryVersion
                val minGradleVersion = project.minGradleVersion()

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:${minGradleVersion.embeddedKotlin}")
                    weak("dev.gradleplugins:gradle-api:${minGradleVersion.gradle}")
                    weak("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$kotlinToolingVersion")
                    weak("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinToolingVersion")
                }
            }
        }
    }
}
