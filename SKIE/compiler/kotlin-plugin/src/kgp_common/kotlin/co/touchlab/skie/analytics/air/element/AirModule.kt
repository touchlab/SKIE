package co.touchlab.skie.analytics.air.element

import co.touchlab.skie.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirModule(
    val name: Name,
    val files: List<AirFile>,
    val isExported: Boolean,
) : AirElement {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirModule =
        transformer.visitModule(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitModule(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        files.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)
}
