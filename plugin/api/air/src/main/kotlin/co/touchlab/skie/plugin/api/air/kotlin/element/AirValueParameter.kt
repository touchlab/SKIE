package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.type.AirType
import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirValueParameter(
    val name: Name,
    /**
     * Corresponds to varargElementType if is vararg
     */
    val type: AirType,
    val hasDefaultValue: Boolean,
    override val containedExpressionSize: Int,
    val annotations: List<AirConstantObject>,
    val origin: AirOrigin,
    val isVararg: Boolean,
    val isCrossinline: Boolean,
    val isNoinline: Boolean,
) : AirElement, AirExpressionContainer {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitValueParameter(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)
}
