package co.touchlab.skie.acceptancetests.framework.internal.skie

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.configuration.ConfigurationScope
import java.nio.file.Path

object TestConfigurationKeys {

    object EnableVerifyFrameworkHeaderPhase : ConfigurationKey.Boolean, ConfigurationScope.Global {

        override val defaultValue: Boolean = true
    }

    object VerifyFrameworkHeaderPhaseSwiftFilePath : ConfigurationKey.Path, ConfigurationScope.Global {

        override val defaultValue: Path
            get() = error("Path must be explicitly provided.")
    }
}
