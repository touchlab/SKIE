package co.touchlab.skie.plugin.api.air.kotlin.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirExpressionContainer {

    val containedExpressionSize: Int
}
