package co.touchlab.skie.plugin.license

data class SkieLicense(val organizationKey: String, val licenseKey: String, val environment: Environment) {

    enum class Environment {
        Production, Dev
    }
}
