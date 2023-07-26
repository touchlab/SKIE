package co.touchlab.skie.analytics.air.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirStatementContainer {

    val containedStatementSize: Int
}
