package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirField(
    val origin: AirOrigin,
    val annotations: List<AirConstantObject>,
    val isFinal: Boolean,
    val isExternal: Boolean,
    val isStatic: Boolean,
    override val containedExpressionSize: Int,
) : AirElement, AirExpressionContainer {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitField(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
    }
}
