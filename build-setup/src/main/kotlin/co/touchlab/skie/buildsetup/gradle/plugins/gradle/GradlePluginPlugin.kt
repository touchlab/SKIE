package co.touchlab.skie.buildsetup.gradle.plugins.gradle

import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleImplicitReceiverPlugin
import co.touchlab.skie.buildsetup.gradle.plugins.utility.UtilityGradleMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.gradlePluginApi
import co.touchlab.skie.buildsetup.util.version.KotlinVersionAttribute
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import co.touchlab.skie.buildsetup.util.version.minGradleVersion
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import com.gradle.publish.PublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class GradlePluginPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlinPlugin>()
        apply<UtilityGradleMinimumTargetKotlinVersionPlugin>()
        apply<UtilityGradleImplicitReceiverPlugin>()
        apply<ShadowPlugin>()
        apply<KotlinPluginWrapper>()
        apply<PublishPlugin>()

        configureDependencies()
        configureMinGradleVersion()
        configureShadow()
    }

    private fun Project.configureDependencies() {
        dependencies {
            compileOnly(gradlePluginApi())
        }
    }

    private fun Project.configureMinGradleVersion() {
        configurations.configureEach {
            if (isCanBeConsumed || isCanBeResolved) {
                attributes {
                    attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(minGradleVersion().gradle))
                }
            }
        }
    }

    private fun Project.configureShadow() {
        tasks.shadowJar.configure {
            archiveClassifier.set("")

            dependencies {
                exclude {
                    it.moduleGroup == "org.jetbrains.kotlin" && it.moduleName.startsWith("kotlin-stdlib")
                }
            }
        }

        SupportedKotlinVersionProvider.getEnabledKotlinVersions(project).forEach { supportedVersion ->
            val safeKotlinVersion = supportedVersion.safeName

            val shimConfiguration = configurations.create("shim-relocation-$safeKotlinVersion") {
                attributes {
                    attribute(KotlinVersionAttribute.attribute, objects.named(supportedVersion.name.toString()))
                    attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
                    attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
                }
            }

            val relocationTask = tasks.register<ShadowJar>("relocate-shim-$safeKotlinVersion") {
                relocate("co.touchlab.skie.plugin.shim.impl", "co.touchlab.skie.plugin.shim.impl_$safeKotlinVersion")
                configurations.set(listOf(shimConfiguration))
                archiveClassifier.set("shim-impl-$safeKotlinVersion")
            }

            tasks.named("compileKotlin").configure {
                dependsOn(relocationTask)
            }

            dependencies {
                shimConfiguration(project(":gradle:gradle-plugin-shim-impl"))
                add("runtimeOnly", relocationTask.map { it.outputs.files })
            }
        }
    }
}
