package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.license.SkieLicense
import co.touchlab.skie.plugin.license.SkieLicenseProvider
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories

object SkieConfigurationProvider {

    fun getConfiguration(skieBuildDirectory: SkieBuildDirectory): Configuration {
        val license = SkieLicenseProvider.loadLicense(skieBuildDirectory)

        val serializedUserConfiguration = skieBuildDirectory.skieConfiguration.readText()
        val userConfiguration = Configuration.deserialize(serializedUserConfiguration)

        val defaultConfiguration = license.configurationFromServer.defaultConfiguration
        val enforcedConfiguration = license.configurationFromServer.enforcedConfiguration

        return defaultConfiguration + userConfiguration + enforcedConfiguration
    }
}
