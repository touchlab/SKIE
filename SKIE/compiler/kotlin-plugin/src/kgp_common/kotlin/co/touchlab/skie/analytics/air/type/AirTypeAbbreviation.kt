package co.touchlab.skie.analytics.air.type

import co.touchlab.skie.analytics.air.element.AirConstantObject
import co.touchlab.skie.analytics.air.element.AirTypeAlias
import kotlinx.serialization.Serializable

@Serializable
data class AirTypeAbbreviation(
    val typeAlias: AirTypeAlias.Symbol,
    val hasQuestionMark: Boolean,
    val arguments: List<AirTypeArgument>,
    val annotations: List<AirConstantObject>,
)