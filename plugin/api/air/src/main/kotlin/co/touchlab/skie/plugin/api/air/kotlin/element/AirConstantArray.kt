package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantArray(
    val elements: List<AirConstant>,
    override val annotations: List<AirConstantObject>,
) : AirConstant {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantArray(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        elements.forEach { it.accept(visitor, data) }
        annotations.forEach { it.accept(visitor, data) }
    }
}
