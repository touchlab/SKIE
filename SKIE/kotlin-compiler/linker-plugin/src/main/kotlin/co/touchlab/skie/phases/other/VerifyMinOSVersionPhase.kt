package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.phases.configurables
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan

object VerifyMinOSVersionPhase : ClassExportPhase {

    context(context: ClassExportPhase.Context)
    override fun isActive(): Boolean = context.run { SkieConfigurationFlag.Feature_CoroutinesInterop.isEnabled }

    context(context: ClassExportPhase.Context)
    override suspend fun execute() {
        val currentMinVersion = context.configurables.osVersionMin
        val minRequiredVersion = getMinRequiredOsVersionForSwiftAsync(context.configurables.target.name)

        if (currentMinVersion.isLowerVersionThan(minRequiredVersion)) {
            error(
                "Minimum OS version for ${context.configurables.target.name} must be at least $minRequiredVersion to support Swift Async. " +
                    "However, the configured minimum OS version is only $currentMinVersion. " +
                    "This is most likely a bug in SKIE Gradle plugin which should have set the minimum required version automatically.",
            )
        }
    }
}
