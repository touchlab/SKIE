package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.phases.ClassExportPhase
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan
import org.jetbrains.kotlin.konan.target.AppleConfigurables

object VerifyMinOSVersionPhase : ClassExportPhase {

    context(ClassExportPhase.Context)
    override fun isActive(): Boolean =
        SkieConfigurationFlag.Feature_CoroutinesInterop in skieConfiguration.enabledConfigurationFlags

    context(ClassExportPhase.Context)
    override fun execute() {
        val configurables = konanConfig.platform.configurables as AppleConfigurables

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
