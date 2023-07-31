package co.touchlab.skie.debug.air.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirStatementContainer {

    val containedStatementSize: Int
}
