package co.touchlab.skie.plugin.util

import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind
import org.jetbrains.kotlin.gradle.targets.native.tasks.artifact.KotlinNativeLinkArtifactTask
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.gradle.utils.named
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.presetName
import org.jetbrains.kotlin.konan.util.visibleName
import java.io.File

sealed interface SkieTarget {
    val name: String
    val project: Project
    val task: TaskProvider<out Task>
    val konanTarget: KonanTarget
    val buildType: NativeBuildType

    val skieDirectories: Provider<SkieDirectories>

    val freeCompilerArgs: Provider<List<String>>

    fun addPluginArgument(pluginId: String, option: SubpluginOption)

    fun addToCompilerClasspath(fileCollection: FileCollection)

    fun addFreeCompilerArgs(vararg args: String)

    class TargetBinary(
        override val project: Project,
        val target: KotlinNativeTarget,
        val binary: NativeBinary,
    ): SkieTarget {
        override val konanTarget: KonanTarget = target.konanTarget

        override val buildType: NativeBuildType = binary.buildType

        override val name: String = "binary: ${binary.name}, target: ${target.targetName}, buildType: $buildType"

        override val task: TaskProvider<out KotlinNativeLink> = binary.linkTaskProvider

        override val skieDirectories = project.layout.buildDirectory.dir("skie/binaries/${binary.name}/$buildType/${binary.target.targetName}").map {
            SkieDirectories(it.asFile)
        }

        override val freeCompilerArgs: Provider<List<String>> = task.map {
            it.binary.freeCompilerArgs
        }

        override fun addPluginArgument(pluginId: String, option: SubpluginOption) {
            task.configure {
                compilerPluginOptions.addPluginArgument(
                    pluginId,
                    option,
                )
            }
        }

        override fun addToCompilerClasspath(fileCollection: FileCollection) {
            task.configure {
                compilerPluginClasspath = listOfNotNull(
                    compilerPluginClasspath,
                    fileCollection,
                ).reduce(FileCollection::plus)
            }
        }

        override fun addFreeCompilerArgs(vararg args: String) {
            task.configure {
                this.binary.freeCompilerArgs += args
            }
        }
    }

    class Artifact(
        override val project: Project,
        val artifact: KotlinNativeArtifact,
        override val konanTarget: KonanTarget,
        override val buildType: NativeBuildType,
    ): SkieTarget {
        override val name: String = "artifact: ${artifact.artifactName}, target: $konanTarget, buildType: $buildType"

        override val task = project.tasks.named<KotlinNativeLinkArtifactTask>(linkTaskName(artifact, konanTarget, buildType))

        override val skieDirectories = project.layout.buildDirectory.dir("skie/artifacts/${artifact.artifactName}/$buildType/$konanTarget").map {
            SkieDirectories(it.asFile)
        }

        override val freeCompilerArgs: Provider<List<String>> = task.flatMap {
            it.toolOptions.freeCompilerArgs
        }

        override fun addPluginArgument(pluginId: String, option: SubpluginOption) {
            task.configure {
                toolOptions.freeCompilerArgs.addAll(
                    "-P",
                    "plugin:${pluginId}:${option.key}=${option.value}"
                )
            }
        }

        override fun addToCompilerClasspath(fileCollection: FileCollection) {
            task.configure {
                toolOptions.freeCompilerArgs.addAll(
                    fileCollection.files.map { it.canonicalPath }.sorted().map { "-Xplugin=$it" }
                )
            }
        }

        override fun addFreeCompilerArgs(vararg args: String) {
            task.configure {
                toolOptions.freeCompilerArgs.addAll(
                    *args
                )
            }
        }

        private companion object {
            fun linkTaskName(artifact: KotlinNativeArtifact, konanTarget: KonanTarget, buildType: NativeBuildType): String {
                return when (artifact) {
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
                    is KotlinNativeFramework -> lowerCamelCaseName(
                        "assemble",
                        artifact.artifactName,
                        buildType.visibleName,
                        NativeOutputKind.FRAMEWORK.taskNameClassifier,
                        konanTarget.presetName
                    )
                    is KotlinNativeFatFramework -> lowerCamelCaseName(
                        "assemble",
                        artifact.artifactName,
                        buildType.visibleName,
                        NativeOutputKind.FRAMEWORK.taskNameClassifier,
                        konanTarget.presetName,
                        "ForFat",
                    )
                    is KotlinNativeXCFramework -> lowerCamelCaseName(
                        "assemble",
                        artifact.artifactName,
                        buildType.visibleName,
                        NativeOutputKind.FRAMEWORK.taskNameClassifier,
                        konanTarget.presetName,
                        "ForXCF",
                    )
                    else -> error("Unknown KotlinNativeArtifact type: $this")
                }
            }
        }
    }
}

val SkieTarget.skieBuildDirectory: Provider<SkieBuildDirectory>
    get() = skieDirectories.map { it.buildDirectory }

val Provider<SkieBuildDirectory>.skieConfiguration: Provider<File>
    get() = map { it.skieConfiguration }
