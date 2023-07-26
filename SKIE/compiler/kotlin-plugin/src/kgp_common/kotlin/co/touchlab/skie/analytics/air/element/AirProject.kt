package co.touchlab.skie.analytics.air.element

import co.touchlab.skie.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirProject(
    val modules: List<AirModule>,
) : AirElement {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirProject =
        transformer.visitProject(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitProject(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        modules.forEach { it.accept(visitor, data) }
    }
}
