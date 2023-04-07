package co.touchlab.skie.plugin.api.air.element

import co.touchlab.skie.plugin.api.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirProject(
    val name: Name,
    val modules: List<AirModule>,
) : AirElement {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitProject(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        modules.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class Name(val name: String)
}
