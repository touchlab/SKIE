package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantArray(
    val elements: List<AirConstant>,
) : AirConstant {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirConstantArray =
        transformer.visitConstantArray(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantArray(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        elements.forEach { it.accept(visitor, data) }
    }
}
