package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.license.SkieLicense
import co.touchlab.skie.plugin.license.SkieLicenseProvider
import co.touchlab.skie.util.directory.SkieDirectories

object SkieConfigurationProvider {

    fun getConfiguration(skieDirectories: SkieDirectories): Configuration {
        val license = SkieLicenseProvider.loadLicense(skieDirectories)

        return getConfiguration(skieDirectories, license)
    }

    fun getConfiguration(skieDirectories: SkieDirectories, skieLicense: SkieLicense): Configuration {
        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()
        val userConfiguration = Configuration.deserialize(serializedUserConfiguration)

        val defaultConfiguration = skieLicense.configurationFromServer.defaultConfiguration
        val enforcedConfiguration = skieLicense.configurationFromServer.enforcedConfiguration

        return defaultConfiguration + userConfiguration + enforcedConfiguration
    }
}
