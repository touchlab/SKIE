package co.touchlab.skie.plugin.analytics.air.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirDeclaration : AirElement {

    val annotations: List<AirConstantObject>

    val origin: AirOrigin
}
