package co.touchlab.skie.plugin.api.air.kotlin.element

import co.touchlab.skie.plugin.api.air.kotlin.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirModule(
    val name: Name,
    val files: List<AirFile>,
) : AirElement {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitModule(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        files.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)
}
