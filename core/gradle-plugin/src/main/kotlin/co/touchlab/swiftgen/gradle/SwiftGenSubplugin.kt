package co.touchlab.swiftgen.gradle

import co.touchlab.swiftgen.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import javax.inject.Inject

abstract class SwiftGenSubplugin @Inject constructor(
    private val project: Project,
) : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        super.apply(target)

        target.extensions.create("swiftGen", SwiftGenExtension::class.java)

        target.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation("co.touchlab.swiftgen:api:${BuildConfig.KOTLIN_PLUGIN_VERSION}")
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val target = kotlinCompilation.target

        return target is KotlinNativeTarget && target.konanTarget.family.isAppleFamily
    }

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        project.configurations.getByName(kotlinCompilation.pluginConfigurationName).dependencies.apply {
            add(project.dependencies.create("co.touchlab.swiftgen:api:${BuildConfig.KOTLIN_PLUGIN_VERSION}"))
            add(project.dependencies.create("co.touchlab.swiftgen:configuration:${BuildConfig.KOTLIN_PLUGIN_VERSION}"))
        }

        return kotlinCompilation.target.project.provider {
            val pluginConfiguration = project.extensions.getByType<SwiftGenExtension>()

            pluginConfiguration.toSubpluginOptions()
        }
    }

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
        version = BuildConfig.KOTLIN_PLUGIN_VERSION,
    )

    private val KotlinCompilation<*>.pluginConfigurationName
        get() = listOfNotNull(PLUGIN_CLASSPATH_CONFIGURATION_NAME, target.disambiguationClassifier, compilationName)
            .withIndex()
            .joinToString("") { (index, value) ->
                if (index == 0) value else value.capitalized()
            }
}