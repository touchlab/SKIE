package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
data class AirFile(
    val packageName: PackageName,
    val fileName: FileName,
    val declarations: List<AirDeclaration>,
    val annotations: List<AirConstantObject>,
) : AirElement {

    override fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R =
        visitor.visitFile(this, data)

    override fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D) {
        declarations.forEach { it.accept(visitor, data) }
        annotations.forEach { it.accept(visitor, data) }
    }

    @Serializable
    data class PackageName(val name: String)

    @Serializable
    data class FileName(val name: String)
}
