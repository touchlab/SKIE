package co.touchlab.skie.plugin.analytics.air.element

import co.touchlab.skie.plugin.analytics.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirElement {

    fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R

    fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D)
}
