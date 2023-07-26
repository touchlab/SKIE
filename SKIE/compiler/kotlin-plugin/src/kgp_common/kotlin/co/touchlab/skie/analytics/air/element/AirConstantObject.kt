package co.touchlab.skie.analytics.air.element

import co.touchlab.skie.analytics.air.type.AirType
import co.touchlab.skie.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantObject(
    val constructor: AirFunction.Symbol,
    val valueArguments: List<AirConstant>,
    val typeArguments: List<AirType>,
) : AirConstant {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirConstantObject =
        transformer.visitConstantObject(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantObject(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        valueArguments.forEach { it.accept(visitor, data) }
    }
}
