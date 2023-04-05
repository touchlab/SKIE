package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.type.AirType
import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirConstructor(
    override val symbol: AirFunction.Symbol,
    override val annotations: List<AirConstantObject>,
    override val origin: AirOrigin,
    override val dispatchReceiverParameter: AirValueParameter?,
    override val extensionReceiverParameter: AirValueParameter?,
    override val valueParameters: List<AirValueParameter>,
    override val typeParameters: List<AirTypeParameter>,
    override val returnType: AirType,
    override val containedExpressionSize: Int,
    val isExported: Boolean,
    val visibility: AirVisibility,
    val isExternal: Boolean,
    val isPrimary: Boolean,
    // val isExpect: Boolean is always false at the time we can collect statistics
    val contextReceiverParametersCount: Int,
) : AirFunction {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitConstructor(this, data)
}
