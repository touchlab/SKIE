package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantClassReference(
    val symbol: AirClass.Symbol,
) : AirConstant {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantClassReference(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
    }
}
