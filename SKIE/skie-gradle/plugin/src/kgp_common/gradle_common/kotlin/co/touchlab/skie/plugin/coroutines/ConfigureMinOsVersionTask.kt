package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary
import org.jetbrains.kotlin.konan.properties.resolvablePropertyString
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.util.DependencyDirectories
import java.util.Properties

internal abstract class ConfigureMinOsVersionTask : DefaultTask() {

    @get:Internal
    abstract val binary: Property<NativeBinary>

    @TaskAction
    fun runTask() {
        val binary = binary.get()
        val konanTarget = binary.target.konanTarget
        val distribution = binary.distribution

        fun overrideVersion(name: String) {
            val currentMinVersion = distribution.properties.targetString(name, konanTarget)
            val minRequiredVersion = getMinRequiredOsVersionForSwiftAsync(konanTarget.name)

            if (currentMinVersion == null || currentMinVersion.isLowerVersionThan(minRequiredVersion)) {
                binary.freeCompilerArgs += listOf("$overrideKonanPropertiesKey=${name}.${konanTarget.name}=$minRequiredVersion")
            }
        }

        overrideVersion("osVersionMin")
        overrideVersion("osVersionMinSinceXcode15")
    }
}

private fun Properties.targetString(name: String, target: KonanTarget): String? =
    resolvablePropertyString(name, target.name)

private const val overrideKonanPropertiesKey = "-Xoverride-konan-properties"

private val NativeBinary.distribution: Distribution
    get() {
        val overrideKonanProperties = parseOverrideKonanProperties(freeCompilerArgs)

        return Distribution(konanHome = DependencyDirectories.localKonanDir.absolutePath, propertyOverrides = overrideKonanProperties)
    }

private fun parseOverrideKonanProperties(
    arguments: List<String>,
): Map<String, String> =
    arguments.associate { it.substringBefore('=') to it.substringAfter('=') }
        .filterKeys { it == overrideKonanPropertiesKey }
        .flatMap { it.value.split(";") }
        .associate { it.substringBefore('=') to it.substringAfter('=') }
