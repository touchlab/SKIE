package co.touchlab.skie.analytics.air.element

import kotlinx.serialization.Serializable

@Serializable
data class AirOrigin(val name: String, val isSynthetic: Boolean)
