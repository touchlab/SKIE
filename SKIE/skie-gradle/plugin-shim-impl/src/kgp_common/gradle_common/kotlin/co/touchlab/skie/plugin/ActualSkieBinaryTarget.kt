package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.shim.ActualKonanTargetShim
import co.touchlab.skie.plugin.shim.ActualKotlinSourceSetShim
import co.touchlab.skie.plugin.shim.KonanTargetShim
import co.touchlab.skie.plugin.shim.KotlinSourceSetShim
import co.touchlab.skie.plugin.util.KotlinCompilerPluginOption
import co.touchlab.skie.util.directory.SkieDirectories
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

class ActualSkieBinaryTarget(
    override val project: Project,
    target: KotlinNativeTarget,
    private val binary: NativeBinary,
    override val outputKind: SkieTarget.OutputKind,
) : SkieTarget.Binary {

    override val konanTarget: KonanTargetShim = ActualKonanTargetShim(target.konanTarget)

    private val buildType: NativeBuildType = binary.buildType

    override val name: String = "binary: ${binary.name}, target: ${target.targetName}, buildType: $buildType"

    override val task: TaskProvider<out KotlinNativeLink> = binary.linkTaskProvider

    override val compileDependencyConfigurationName: String
        get() = binary.compilation.compileDependencyConfigurationName

    override val allCompilationKotlinSourceSets: List<KotlinSourceSetShim>
        get() = binary.compilation.allKotlinSourceSets.map { ActualKotlinSourceSetShim(it) }

    override val skieDirectories: Provider<SkieDirectories> =
        project.layout.buildDirectory
            .dir("skie/binaries/${binary.name}/$buildType/${binary.target.targetName}")
            .map { SkieDirectories(it.asFile) }

    override val freeCompilerArgs: Provider<List<String>> = task.map {
        it.binary.freeCompilerArgs
    }

    override fun addPluginArgument(pluginId: String, option: KotlinCompilerPluginOption) {
        task.configure {
            compilerPluginOptions.addPluginArgument(
                pluginId,
                SubpluginOption(option.key, option.value),
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
