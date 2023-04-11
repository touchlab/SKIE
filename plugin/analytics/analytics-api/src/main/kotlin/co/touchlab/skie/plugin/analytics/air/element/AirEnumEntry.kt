package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementTransformer
import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirEnumEntry(
    val symbol: Symbol,
    val name: Name,
    val enumEntryClass: AirClass?,
    val origin: AirOrigin,
    val annotations: List<AirConstantObject>,
    override val containedStatementSize: Int,
) : AirElement, AirStatementContainer {

    override fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirEnumEntry =
        transformer.visitEnumEntry(this, data)

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitEnumEntry(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        enumEntryClass?.accept(visitor, data)
        annotations.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)

    @Serializable
    data class Symbol(val id: Int)
}
