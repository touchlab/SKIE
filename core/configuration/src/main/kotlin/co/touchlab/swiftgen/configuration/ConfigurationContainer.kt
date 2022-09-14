package co.touchlab.swiftgen.configuration

interface ConfigurationContainer {

    val configuration: Configuration

    fun <T> ConfigurationTarget.getConfiguration(key: ConfigurationKey<T>): T =
        configuration[this, key]
}