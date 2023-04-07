package co.touchlab.skie.plugin.api.air.element

import co.touchlab.skie.plugin.api.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantPrimitive(
    val value: String,
    val kind: Kind,
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
    }
}
