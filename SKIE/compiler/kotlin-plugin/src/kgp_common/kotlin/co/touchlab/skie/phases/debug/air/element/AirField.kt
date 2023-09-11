package co.touchlab.skie.phases.debug.air.element

import co.touchlab.skie.phases.debug.air.type.AirType
import co.touchlab.skie.phases.debug.air.visitor.AirElementTransformer
import co.touchlab.skie.phases.debug.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirField(
    val name: Name,
    override val origin: AirOrigin,
    override val annotations: List<AirConstantObject>,
    val type: AirType,
    val visibility: AirVisibility,
    val isFinal: Boolean,
    val isExternal: Boolean,
    val isStatic: Boolean,
    override val containedStatementSize: Int,
) : AirDeclaration, AirStatementContainer {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirField =
        transformer.visitField(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitField(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)
}
