package co.touchlab.skie.phases.debug.air.element

import co.touchlab.skie.phases.debug.air.type.AirType
import co.touchlab.skie.phases.debug.air.visitor.AirElementTransformer
import co.touchlab.skie.phases.debug.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirSimpleFunction(
    override val symbol: AirFunction.Symbol,
    override val annotations: List<AirConstantObject>,
    override val origin: AirOrigin,
    override val dispatchReceiverParameter: AirValueParameter?,
    override val extensionReceiverParameter: AirValueParameter?,
    override val valueParameters: List<AirValueParameter>,
    override val typeParameters: List<AirTypeParameter>,
    override val returnType: AirType,
    override val containedStatementSize: Int,
    val name: Name,
    val isExported: Boolean,
    val visibility: AirVisibility,
    val modality: AirModality,
    val isInline: Boolean,
    val isExternal: Boolean,
    val isTailrec: Boolean,
    val isSuspend: Boolean,
    val isOperator: Boolean,
    val isInfix: Boolean,
    val isExpect: Boolean,
    val isFakeOverride: Boolean,
    val overriddenSymbols: List<AirFunction.Symbol>,
    val contextReceiverParametersCount: Int,
) : AirFunction {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirSimpleFunction =
        transformer.visitSimpleFunction(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitSimpleFunction(this, data)

    @Serializable
    data class Name(val name: String)
}
