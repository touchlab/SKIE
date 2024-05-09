package co.touchlab.skie.util

data class TargetTriple(
    val architecture: String,
    val vendor: String,
    val os: String,
    val environment: String?,
) {

    override fun toString(): String {
        val envSuffix = environment?.let { "-$environment" } ?: ""

        return "$architecture-$vendor-$os$envSuffix"
    }

    fun withOsVersion(osVersion: String): TargetTriple =
        copy(os = "$os$osVersion")

    companion object {

        operator fun invoke(tripleString: String): TargetTriple {
            val components = tripleString.split('-')

            require(components.size in 3..4) {
                "Invalid target triple: $tripleString, should be <arch>-<vendor>-<os>-<environment?>."
            }

            return TargetTriple(
                architecture = components[0],
                vendor = components[1],
                os = components[2],
                environment = components.getOrNull(3),
            )
        }
    }
}
