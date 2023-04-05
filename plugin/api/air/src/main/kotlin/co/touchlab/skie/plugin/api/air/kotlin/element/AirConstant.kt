package co.touchlab.skie.plugin.api.air.kotlin.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirConstant : AirElement {

    val annotations: List<AirConstantObject>
}
