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
        apply<KotlinMultiplatformPluginWrapper>()
        apply<MultiDimensionTargetPlugin>()

        group = "co.touchlab.skie"

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(libs.versions.java)

//             val kotlinVersions = kotlinToolingVersions()
//             with(kotlinVersions) {
//                 val sourceDir = project.file("src").toPath()
//                 val testsDir = project.file("tests").toPath()
//
//                 val versionRanges = sourceSetRangesIn(sourceDir, testsDir)
//                 val rootTarget = SourceSetScope.Target.Root(oldest)
//                 val commonTargets = commonTargets()
//                 val leafTargets = cells.map {
//                     SourceSetScope.Target.Leaf(it)
//                 }
//                 val rangeTargets = versionRanges.map {
//                     SourceSetScope.Target.Range(it)
//                 }
//
//                 val targetsByKotlinVersion = cells.associateWith { version ->
//                     apple(version)
//                 }
//
//                 SourceSetScope.Compilation.values().flatMap { compilation ->
//                     val root = prepare(rootTarget, compilation, this)
//                     val common = commonTargets.map { prepare(it, compilation, this) }
//                     val ranges = rangeTargets.map { prepare(it, compilation, this) }
//                     val leaves = leafTargets.map { prepare(it, compilation, this) }
//
//                     val all = listOf(listOf(root), common, ranges, leaves).flatten()
//                     all.forEach { sourceSet ->
//                         sourceSet.apply {
//                             val root = when (compilation) {
//                                 SourceSetScope.Compilation.Main -> sourceDir.name
//                                 SourceSetScope.Compilation.Test -> testsDir.name
//                             }
//                             val path = sourceSet.target.pathComponents().joinToString("/")
//                             kotlinSourceSet.kotlin.setSrcDirs(
//                                 listOf("$root/$path/kotlin")
//                             )
//                             kotlinSourceSet.resources.setSrcDirs(
//                                 listOf("$root/$path/resources")
//                             )
//                         }
//                     }
//
//                     matrix.configureSourceSetHierarchy(root, common, ranges, leaves)
//
//                     all
//                 }
//             }
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
