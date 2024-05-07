package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.kgpShim
import co.touchlab.skie.plugin.shim.KonanTargetShim
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan
import org.gradle.api.Project
import java.util.Properties

internal fun SkieTarget.configureMinOsVersionIfNeeded() {
    project.kgpShim.launchScheduler.afterEvaluateOrAfterFinaliseRefinesEdges(project) {
        if (!project.isCoroutinesInteropEnabled) {
            return@afterEvaluateOrAfterFinaliseRefinesEdges
        }

        val distributionProperties = getDistributionProperties()

        fun overrideVersion(name: String) {
            val currentMinVersion = distributionProperties.targetString(name, konanTarget, project)
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
        konanHome = project.kgpShim.getKonanHome().absolutePath,
        propertyOverrides = parseOverrideKonanProperties(freeCompilerArgs.get()),
    )

private fun Properties.targetString(name: String, target: KonanTargetShim, project: Project): String? =
    project.kgpShim.resolvablePropertyString(this, name, target.name)

private const val overrideKonanPropertiesKey = "-Xoverride-konan-properties"

private fun parseOverrideKonanProperties(
    arguments: List<String>,
): Map<String, String> =
    arguments.associate { it.substringBefore('=') to it.substringAfter('=') }
        .filterKeys { it == overrideKonanPropertiesKey }
        .flatMap { it.value.split(";") }
        .associate { it.substringBefore('=') to it.substringAfter('=') }
