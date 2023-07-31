package co.touchlab.skie.debug.air.element

import co.touchlab.skie.debug.air.visitor.AirElementTransformer
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirConstant : AirElement {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirConstant
}
