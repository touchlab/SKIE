package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.shim.KonanTargetShim
import co.touchlab.skie.plugin.shim.KotlinNativeCompilationShim
import co.touchlab.skie.plugin.util.KotlinCompilerPluginOption
import co.touchlab.skie.util.directory.SkieDirectories
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
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
    binary: NativeBinary,
    override val compilationProvider: Provider<KotlinNativeCompilationShim>,
    private val isForXCFramework: Boolean,
) : SkieTarget.Binary {

    override val konanTarget: KonanTargetShim = ActualKonanTargetShim(target.konanTarget)

    private val buildType: NativeBuildType = binary.buildType

    override val name: String = "binary: ${binary.name}, target: ${target.targetName}, buildType: $buildType"

    override val outputKind: SkieTarget.OutputKind = SkieTarget.OutputKind.Framework

    override val task: TaskProvider<out KotlinNativeLink> = binary.linkTaskProvider

    override val linkerConfiguration: Configuration = project.configurations.getByName(binary.compilation.compileDependencyConfigurationName)

    override val skieDirectories: Provider<SkieDirectories> =
        project.layout.buildDirectory
            .dir("skie/binaries/${binary.name}/$buildType/${binary.target.targetName}")
            .map { SkieDirectories(it.asFile) }

    override val freeCompilerArgs: Provider<List<String>> = task.map {
        it.binary.freeCompilerArgs
    }

    override val requiredConfigurationFlags: Set<SkieConfigurationFlag> = setOfNotNull(
        SkieConfigurationFlag.Build_SwiftLibraryEvolution.takeIf { isForXCFramework },
    )

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

    override fun addFreeCompilerArgsImmediately(vararg args: String) {
        task.get().binary.freeCompilerArgs += args
    }
}
