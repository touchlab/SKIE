package co.touchlab.skie.acceptancetests.framework.internal.skie

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.spi.SkiePluginRegistrar

class TestSkiePhasesRegistrar : SkiePluginRegistrar {

    override val customConfigurationKeys: Set<ConfigurationKey<*>> = setOf(
        TestConfigurationKeys.EnableVerifyFrameworkHeaderPhase,
        TestConfigurationKeys.VerifyFrameworkHeaderPhaseSwiftFilePath,
    )

    override fun register(initPhaseContext: InitPhase.Context) {
        initPhaseContext.skiePhaseScheduler.sirPhases.modify {
            add(VerifyTestPhasesAreExecutedPhase)
            add(VerifyFrameworkHeaderPhase)
        }
    }
}
