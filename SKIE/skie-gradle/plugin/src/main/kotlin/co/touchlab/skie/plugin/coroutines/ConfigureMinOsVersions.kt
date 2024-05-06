package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.plugin.kgpShim
import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan
import org.jetbrains.kotlin.konan.properties.resolvablePropertyString
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.util.Properties

internal fun SkieTarget.configureMinOsVersionIfNeeded() {
    project.kgpShim.launchScheduler.afterEvaluateOrAfterFinaliseRefinesEdges(project) {
        if (!project.isCoroutinesInteropEnabled) {
            return@afterEvaluateOrAfterFinaliseRefinesEdges
        }

        val distributionProperties = getDistributionProperties()

        fun overrideVersion(name: String) {
            val currentMinVersion = distributionProperties.targetString(name, konanTarget)
            val minRequiredVersion = getMinRequiredOsVersionForSwiftAsync(konanTarget.name)

            if (currentMinVersion == null || currentMinVersion.isLowerVersionThan(minRequiredVersion)) {
                addFreeCompilerArgs("$overrideKonanPropertiesKey=${name}.${konanTarget.name}=$minRequiredVersion")
            }
        }

        overrideVersion("osVersionMin")
        overrideVersion("osVersionMinSinceXcode15")
    }
}

private fun SkieTarget.getDistributionProperties(): Properties =
    project.kgpShim.getDistributionProperties(
        konanHome = project.kgpShim.getKonanHome(project).absolutePath,
        propertyOverrides = parseOverrideKonanProperties(freeCompilerArgs.get()),
    )

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
