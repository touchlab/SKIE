package co.touchlab.skie.analytics.air.element

import co.touchlab.skie.analytics.air.type.AirType
import co.touchlab.skie.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirTypeAlias(
    val symbol: Symbol,
    val name: Name,
    override val origin: AirOrigin,
    override val annotations: List<AirConstantObject>,
    val visibility: AirVisibility,
    val typeParameters: List<AirTypeParameter>,
    val isActual: Boolean,
    val expandedType: AirType,
) : AirDeclaration {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirTypeAlias =
        transformer.visitTypeAlias(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitTypeAlias(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
        typeParameters.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)

    @Serializable
    data class Symbol(val id: Int)
}
