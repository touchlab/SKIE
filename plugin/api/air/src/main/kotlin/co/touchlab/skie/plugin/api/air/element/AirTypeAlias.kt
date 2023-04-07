package co.touchlab.skie.plugin.api.air.element

import co.touchlab.skie.plugin.api.air.type.AirType
import co.touchlab.skie.plugin.api.air.visitor.AirElementVisitor
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

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitTypeAlias(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
        typeParameters.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)

    @Serializable
    @JvmInline
    value class Symbol(val id: Int)
}
