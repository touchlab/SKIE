package co.touchlab.skie.plugin.generator.internal.coroutines

import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import co.touchlab.skie.util.version.getMinRequiredOsVersionForSwiftAsync
import co.touchlab.skie.util.version.isLowerVersionThan
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.konan.target.AppleConfigurables

class VerifyMinOSVersionPhase(
    private val configuration: SkieConfiguration,
    private val konanConfig: KonanConfig,
) : SkieCompilationPhase {

    override val isActive: Boolean
        get() = SkieConfigurationFlag.Feature_CoroutinesInterop in configuration.enabledConfigurationFlags

    override fun runObjcPhase() {
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
