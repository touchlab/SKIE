package co.touchlab.skie.buildsetup.tests.plugins.tests

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityBuildConfigPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityExperimentalContextReceiversPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityOptInExperimentalCompilerApiPlugin
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersion
import co.touchlab.skie.buildsetup.util.version.KotlinToolingVersionProvider
import co.touchlab.skie.buildsetup.util.compileOnly
import co.touchlab.skie.buildsetup.util.implementation
import co.touchlab.skie.buildsetup.util.kotlinNativeCompilerHome
import co.touchlab.skie.buildsetup.util.withKotlinNativeCompilerEmbeddableDependency
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class TestsAcceptanceTestsFrameworkPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<BaseKotlinPlugin>()
        apply<UtilityMinimumTargetKotlinVersionPlugin>()
        apply<UtilityOptInExperimentalCompilerApiPlugin>()
        apply<UtilityExperimentalContextReceiversPlugin>()
        apply<UtilityBuildConfigPlugin>()
        apply<KotlinPluginWrapper>()

        val activeKotlinVersion = KotlinToolingVersionProvider.getActiveKotlinToolingVersion(project).primaryVersion

        configureBuildConfig(activeKotlinVersion)
        configureDependencies(activeKotlinVersion)
    }

    private fun Project.configureBuildConfig(activeKotlinVersion: KotlinToolingVersion) {
        val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer
        val mainSourceSet = sourceSets.named("main")

        extensions.configure<BuildConfigExtension> {
            generator.set(BuildConfigKotlinGenerator(internalVisibility = false))

            buildConfigField(
                type = "String",
                name = "KONAN_HOME",
                value = "\"${project.kotlinNativeCompilerHome(activeKotlinVersion).path}\"",
            )

            buildConfigField(
                type = "String",
                name = "RESOURCES",
                value = mainSourceSet.map { "\"${it.output.resourcesDir!!.absolutePath}\"" },
            )
        }
    }

    private fun Project.configureDependencies(activeKotlinVersion: KotlinToolingVersion) {
        dependencies {
            withKotlinNativeCompilerEmbeddableDependency(activeKotlinVersion) { dependency ->
                compileOnly(dependency)
            }

            val trove4j = project.kotlinNativeCompilerHome(activeKotlinVersion).resolve("konan/lib/trove4j.jar")

            implementation(files(trove4j))
        }
    }
}
