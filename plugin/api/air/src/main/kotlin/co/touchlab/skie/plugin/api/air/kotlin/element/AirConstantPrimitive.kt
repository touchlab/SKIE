package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantPrimitive(
    val value: String,
    val kind: Kind,
    override val annotations: List<AirConstantObject>,
) : AirConstant {

    enum class Kind {
        Null,
        Boolean,
        Char,
        Byte,
        Short,
        Int,
        Long,
        String,
        Float,
        Double,
    }

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantPrimitive(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
    }
}
