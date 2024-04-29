package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.plugin.shim.ShimEntrypoint
import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.plugin.util.getKonanHome
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.konan.properties.resolvablePropertyString
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.Properties

internal fun SkieTarget.configureMinOsVersionIfNeeded(shims: ShimEntrypoint) = with(shims.launchScheduler) {
    project.afterEvaluateOrAfterFinaliseRefinesEdges {
        if (!project.isCoroutinesInteropEnabled) {
            return@afterEvaluateOrAfterFinaliseRefinesEdges
        }

        val distribution = distribution(shims).get()

        fun overrideVersion(name: String) {
            val currentMinVersion = distribution.properties.targetString(name, konanTarget)
            val minRequiredVersion = getMinRequiredOsVersionForSwiftAsync(konanTarget.name)

            if (currentMinVersion == null || currentMinVersion.isLowerVersionThan(minRequiredVersion)) {
                addFreeCompilerArgs("$overrideKonanPropertiesKey=${name}.${konanTarget.name}=$minRequiredVersion")
            }
        }

        overrideVersion("osVersionMin")
        overrideVersion("osVersionMinSinceXcode15")
    }
}

private fun SkieTarget.distribution(shims: ShimEntrypoint): Provider<Distribution> = freeCompilerArgs.map {
    val overrideKonanProperties = parseOverrideKonanProperties(it)
    shims.distributionProvider.provideDistribution(
        konanHome = project.getKonanHome(shims).absolutePath,
        propertyOverrides = overrideKonanProperties,
    )
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
