package co.touchlab.skie.plugin.api.air.element

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirStatementContainer {

    val containedStatementSize: Int
}
