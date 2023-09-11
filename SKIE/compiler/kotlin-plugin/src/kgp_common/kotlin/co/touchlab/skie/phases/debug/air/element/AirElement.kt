package co.touchlab.skie.phases.debug.air.element

import co.touchlab.skie.phases.debug.air.visitor.AirElementTransformer
import co.touchlab.skie.phases.debug.air.visitor.AirElementVisitor
import kotlinx.serialization.Serializable

@Serializable
sealed interface AirElement {

    fun <R, D> accept(visitor: AirElementVisitor<R, D>, data: D): R

    fun <D> acceptChildren(visitor: AirElementVisitor<Unit, D>, data: D)

    fun <D> transform(transformer: AirElementTransformer<D>, data: D): AirElement
}
