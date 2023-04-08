package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantEnumReference(
    val symbol: AirEnumEntry.Symbol,
) : AirConstant {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantEnumReference(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
    }
}
