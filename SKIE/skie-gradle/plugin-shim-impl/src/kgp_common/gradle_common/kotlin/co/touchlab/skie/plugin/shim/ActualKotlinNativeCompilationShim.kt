package co.touchlab.skie.plugin.shim

import co.touchlab.skie.util.directory.SkieCompilationDirectory
import org.gradle.api.DomainObjectSet
import org.gradle.api.Named
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import java.io.File

class ActualKotlinNativeCompilationShim(
    val kotlinNativeCompilation: KotlinNativeCompilation,
    override val target: KotlinNativeTargetShim,
) : KotlinNativeCompilationShim, Named by kotlinNativeCompilation {

    override val compileTaskProvider: TaskProvider<out Task> = kotlinNativeCompilation.compileTaskProvider

    override val compileTaskOutputFileProvider: Provider<File> = kotlinNativeCompilation.compileTaskProvider.flatMap { it.outputFile }

    override val allKotlinSourceSets: DomainObjectSet<KotlinSourceSetShim> =
        kotlinNativeCompilation.project.objects.domainObjectSet(KotlinSourceSetShim::class.java)

    override val skieCompilationDirectory: Provider<SkieCompilationDirectory> =
        kotlinNativeCompilation.project.layout.buildDirectory
            .dir("skie/compilation/${target.name}/${kotlinNativeCompilation.name}")
            .map { SkieCompilationDirectory(it.asFile) }

    init {
        kotlinNativeCompilation.allKotlinSourceSets.forAll {
            allKotlinSourceSets.add(ActualKotlinSourceSetShim(it))
        }
    }

    override fun toString(): String =
        kotlinNativeCompilation.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActualKotlinNativeCompilationShim) return false

        if (kotlinNativeCompilation != other.kotlinNativeCompilation) return false

        return true
    }

    override fun hashCode(): Int =
        kotlinNativeCompilation.hashCode()
}
