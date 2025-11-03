@file:Suppress("DEPRECATION")

package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.shim.KonanTargetShim
import co.touchlab.skie.plugin.util.KotlinCompilerPluginOption
import co.touchlab.skie.plugin.util.lowerCamelCaseName
import co.touchlab.skie.util.directory.SkieDirectories
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeArtifact
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeFatFramework
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeFramework
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeLibrary
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeXCFramework
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind
import org.jetbrains.kotlin.gradle.targets.native.tasks.artifact.KotlinNativeLinkArtifactTask
import org.jetbrains.kotlin.gradle.utils.named
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.visibleName

class ActualSkieArtifactTarget(
    override val project: Project,
    private val artifact: KotlinNativeArtifact,
    private val actualKonanTarget: KonanTarget,
    private val buildType: NativeBuildType,
    override val outputKind: SkieTarget.OutputKind,
) : SkieTarget.Artifact {

    override val name: String = "artifact: ${artifact.artifactName}, target: $actualKonanTarget, buildType: $buildType"

    override val konanTarget: KonanTargetShim = ActualKonanTargetShim(actualKonanTarget)

    override val fullArtifactName: String
        get() = artifact.artifactName + artifactNameSuffix

    override val requiredConfigurationFlags: Set<SkieConfigurationFlag> = setOfNotNull(
        SkieConfigurationFlag.Build_SwiftLibraryEvolution.takeIf { outputKind == SkieTarget.OutputKind.XCFramework },
    )

    private val artifactNameSuffix = when (artifact) {
        is KotlinNativeFatFramework -> "ForFat"
        is KotlinNativeXCFramework -> "ForXCF"
        else -> ""
    }

    private val linkTaskName = when (artifact) {
        is KotlinNativeLibrary -> {
            val kind = if (artifact.isStatic) NativeOutputKind.STATIC else NativeOutputKind.DYNAMIC
            lowerCamelCaseName(
                "assemble",
                artifact.artifactName,
                buildType.visibleName,
                kind.taskNameClassifier,
                "Library",
                konanTarget.presetName,
            )
        }
        is KotlinNativeFramework, is KotlinNativeFatFramework, is KotlinNativeXCFramework -> lowerCamelCaseName(
            "assemble",
            artifact.artifactName,
            buildType.visibleName,
            NativeOutputKind.FRAMEWORK.taskNameClassifier,
            konanTarget.presetName,
            artifactNameSuffix,
        )
        else -> error("Unknown KotlinNativeArtifact type: $this")
    }

    override val task = project.tasks.named<KotlinNativeLinkArtifactTask>(linkTaskName)

    override val skieDirectories: Provider<SkieDirectories> =
        project.layout.buildDirectory
            .dir("skie/artifacts/${artifact.artifactName}/$buildType/$konanTarget")
            .map { SkieDirectories(it.asFile) }

    override val freeCompilerArgs: Provider<List<String>> = task.flatMap {
        it.toolOptions.freeCompilerArgs
    }

    override val linkerConfiguration: Configuration = project.configurations.getByName(
        lowerCamelCaseName(konanTarget.presetName, fullArtifactName, "linkLibrary"),
    )

    override fun addPluginArgument(pluginId: String, option: KotlinCompilerPluginOption) {
        task.configure {
            toolOptions.freeCompilerArgs.addAll(
                "-P",
                "plugin:${pluginId}:${option.key}=${option.value}",
            )
        }
    }

    override fun addToCompilerClasspath(fileCollection: FileCollection) {
        task.configure {
            toolOptions.freeCompilerArgs.addAll(
                project.provider {
                    fileCollection.files.map { it.canonicalPath }.sorted().map { "-Xplugin=$it" }
                },
            )
        }
    }

    override fun addFreeCompilerArgsImmediately(vararg args: String) {
        task.get().toolOptions.freeCompilerArgs.addAll(*args)
    }

    override fun addLinkerTaskInputs(configuration: Configuration) {
        task.configure {
            inputs.files(configuration)
        }
    }

    companion object {

        fun createFromArtifact(artifact: KotlinNativeArtifact, project: Project): List<SkieTarget> =
            when (artifact) {
                is KotlinNativeLibrary -> createFromArtifact(artifact, project)
                is KotlinNativeFramework -> createFromArtifact(artifact, project)
                is KotlinNativeFatFramework -> createFromArtifact(artifact, project)
                is KotlinNativeXCFramework -> createFromArtifact(artifact, project)
                else -> error("Unknown KotlinNativeArtifact type: $this")
            }

        private fun createFromArtifact(artifact: KotlinNativeLibrary, project: Project): List<ActualSkieArtifactTarget> =
            if (artifact.target.family.isAppleFamily) {
                artifact.modes.map { buildType ->
                    ActualSkieArtifactTarget(
                        project = project,
                        artifact = artifact,
                        actualKonanTarget = artifact.target,
                        buildType = buildType,
                        outputKind = SkieTarget.OutputKind.Library,
                    )
                }
            } else {
                emptyList()
            }

        private fun createFromArtifact(artifact: KotlinNativeFramework, project: Project): List<ActualSkieArtifactTarget> =
            if (artifact.target.family.isAppleFamily) {
                artifact.modes.map { buildType ->
                    ActualSkieArtifactTarget(
                        project = project,
                        artifact = artifact,
                        actualKonanTarget = artifact.target,
                        buildType = buildType,
                        outputKind = SkieTarget.OutputKind.Framework,
                    )
                }
            } else {
                emptyList()
            }

        private fun createFromArtifact(artifact: KotlinNativeFatFramework, project: Project): List<ActualSkieArtifactTarget> =
            artifact.modes.flatMap { buildType ->
                artifact.targets.filter { it.family.isAppleFamily }.map { target ->
                    ActualSkieArtifactTarget(
                        project = project,
                        artifact = artifact,
                        actualKonanTarget = target,
                        buildType = buildType,
                        outputKind = SkieTarget.OutputKind.UniversalFramework,
                    )
                }
            }

        private fun createFromArtifact(artifact: KotlinNativeXCFramework, project: Project): List<ActualSkieArtifactTarget> =
            artifact.modes.flatMap { buildType ->
                artifact.targets.filter { it.family.isAppleFamily }.map { target ->
                    ActualSkieArtifactTarget(
                        project = project,
                        artifact = artifact,
                        actualKonanTarget = target,
                        buildType = buildType,
                        outputKind = SkieTarget.OutputKind.XCFramework,
                    )
                }
            }
    }
}
