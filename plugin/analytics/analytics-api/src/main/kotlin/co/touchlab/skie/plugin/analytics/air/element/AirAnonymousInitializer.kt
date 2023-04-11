package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirAnonymousInitializer(
    override val annotations: List<AirConstantObject>,
    override val origin: AirOrigin,
    override val containedStatementSize: Int,
    val isStatic: Boolean,
) : AirDeclaration, AirStatementContainer {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirAnonymousInitializer =
        transformer.visitAnonymousInitializer(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitAnonymousInitializer(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        annotations.forEach { it.accept(visitor, data) }
    }
}
