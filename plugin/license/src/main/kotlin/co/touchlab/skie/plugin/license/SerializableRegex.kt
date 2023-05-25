package co.touchlab.skie.plugin.license

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class SerializableRegex(val value: String) {

    val regex: Regex
        get() = value.toRegex()
}
