package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.configurables
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan

object VerifyMinOSVersionPhase : ClassExportPhase {

    context(ClassExportPhase.Context)
    override fun isActive(): Boolean = SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled

    context(ClassExportPhase.Context)
    override suspend fun execute() {
        val currentMinVersion = configurables.osVersionMin
        val minRequiredVersion = getMinRequiredOsVersionForSwiftAsync(configurables.target.name)

        if (currentMinVersion.isLowerVersionThan(minRequiredVersion)) {
            error(
                "Minimum OS version for ${configurables.target.name} must be at least $minRequiredVersion to support Swift Async. " +
                    "However, the configured minimum OS version is only $currentMinVersion. " +
                    "This is most likely a bug in SKIE Gradle plugin which should have set the minimum required version automatically.",
            )
        }
    }
}
