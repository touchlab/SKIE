package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.type.AirType
import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstantObject(
    val constructor: AirFunction.Symbol,
    val valueArguments: List<AirConstant>,
    val typeArguments: List<AirType>,
    override val annotations: List<AirConstantObject>,
) : AirConstant {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstantObject(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        valueArguments.forEach { it.accept(visitor, data) }
        annotations.forEach { it.accept(visitor, data) }
    }
}
