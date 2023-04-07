package co.touchlab.skie.plugin.api.air.element

import kotlinx.serialization.Serializable

@Serializable
data class AirOrigin(val name: String, val isSynthetic: Boolean)
