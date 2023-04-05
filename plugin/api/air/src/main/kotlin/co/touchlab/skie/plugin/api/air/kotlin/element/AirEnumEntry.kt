package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirEnumEntry(
    val name: Name,
    val enumEntryClass: AirClass,
    val origin: AirOrigin,
    val annotations: List<AirConstantObject>,
    override val containedExpressionSize: Int,
) : AirElement, AirExpressionContainer {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitEnumEntry(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        enumEntryClass.accept(visitor, data)
        annotations.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)
}
