package co.touchlab.skie.plugin.shim

import co.touchlab.skie.util.directory.SkieCompilationDirectory
import java.io.File
import org.gradle.api.DomainObjectSet
import org.gradle.api.Named
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

interface KotlinNativeCompilationShim : Named {

    val target: KotlinNativeTargetShim

    val compileTaskProvider: TaskProvider<out Task>

    val compileTaskOutputFileProvider: Provider<File>

    val allKotlinSourceSets: DomainObjectSet<KotlinSourceSetShim>

    val skieCompilationDirectory: Provider<SkieCompilationDirectory>
}
