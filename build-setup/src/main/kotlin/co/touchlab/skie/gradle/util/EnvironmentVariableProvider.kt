package co.touchlab.skie.gradle.util

class EnvironmentVariableProvider(private val name: String) {

    private val value: String? = System.getenv(name)

    val valueOrEmpty: String = value ?: ""

    fun verifyWasSet() {
        if (value == null) {
            throw IllegalStateException("Missing environment variable \"$name\"")
        }
    }
}
