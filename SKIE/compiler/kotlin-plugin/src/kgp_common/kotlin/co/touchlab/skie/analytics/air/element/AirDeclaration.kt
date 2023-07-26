package co.touchlab.skie.analytics.air.element

import co.touchlab.skie.analytics.air.visitor.AirElementTransformer
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirDeclaration : AirElement {

    val annotations: List<AirConstantObject>

    val origin: AirOrigin

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirDeclaration
}
