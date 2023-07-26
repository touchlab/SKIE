package co.touchlab.skie.analytics.air.element

import co.touchlab.skie.analytics.air.visitor.AirElementTransformer
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirConstant : AirElement {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirConstant
}
