package co.touchlab.skie.plugin.api.air.kotlin.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirDeclaration : AirElement {

    val annotations: List<AirConstantObject>

    val origin: AirOrigin
}
