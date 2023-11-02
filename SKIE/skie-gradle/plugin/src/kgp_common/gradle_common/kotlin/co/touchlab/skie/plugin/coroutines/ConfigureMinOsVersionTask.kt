package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.plugin.util.getKonanHome
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.gradle.targets.native.tasks.artifact.KotlinNativeLinkArtifactTask
import org.jetbrains.kotlin.konan.properties.resolvablePropertyString
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.Properties

internal abstract class ConfigureMinOsVersionTask : DefaultTask() {

    @get:Internal
    abstract val target: Property<SkieTarget>

    @TaskAction
    fun runTask() {
        val target = target.get()
        val konanTarget = target.konanTarget
        val distribution = target.distribution.get()

        fun overrideVersion(name: String) {
            val currentMinVersion = distribution.properties.targetString(name, konanTarget)
            val minRequiredVersion = getMinRequiredOsVersionForSwiftAsync(konanTarget.name)

            if (currentMinVersion == null || currentMinVersion.isLowerVersionThan(minRequiredVersion)) {
                target.addFreeCompilerArgs("$overrideKonanPropertiesKey=${name}.${konanTarget.name}=$minRequiredVersion")
            }
        }

        overrideVersion("osVersionMin")
        overrideVersion("osVersionMinSinceXcode15")
    }

    sealed interface NativeBinaryOrArtifact {
        data class Binary(val binary: NativeBinary): NativeBinaryOrArtifact
        data class Artifact(val task: KotlinNativeLinkArtifactTask): NativeBinaryOrArtifact

        val project: Project
            get() = when (this) {
                is Binary -> binary.project
                is Artifact -> task.project
            }

        val konanTarget: KonanTarget
            get() = when (this) {
                is Binary -> binary.target.konanTarget
                is Artifact -> task.konanTarget
            }

        var freeCompilerArgs: List<String>
            get() = when (this) {
                is Binary -> binary.freeCompilerArgs
                is Artifact -> task.toolOptions.freeCompilerArgs.get()
            }
            set(value) {
                when (this) {
                    is Binary -> binary.freeCompilerArgs = value
                    is Artifact -> task.toolOptions.freeCompilerArgs.set(value)
                }
            }


    }
}

private val SkieTarget.distribution: Provider<Distribution>
    get() = freeCompilerArgs.map {
        val overrideKonanProperties = parseOverrideKonanProperties(it)
        Distribution(konanHome = project.getKonanHome().absolutePath, propertyOverrides = overrideKonanProperties)
    }

private fun Properties.targetString(name: String, target: KonanTarget): String? =
    resolvablePropertyString(name, target.name)

private const val overrideKonanPropertiesKey = "-Xoverride-konan-properties"


private fun parseOverrideKonanProperties(
    arguments: List<String>,
): Map<String, String> =
    arguments.associate { it.substringBefore('=') to it.substringAfter('=') }
        .filterKeys { it == overrideKonanPropertiesKey }
        .flatMap { it.value.split(";") }
        .associate { it.substringBefore('=') to it.substringAfter('=') }
