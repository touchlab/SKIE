package co.touchlab.skie.analytics.air.element

import co.touchlab.skie.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantEnumReference(
    val symbol: AirEnumEntry.Symbol,
) : AirConstant {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirConstantEnumReference =
        transformer.visitConstantEnumReference(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantEnumReference(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
    }
}
