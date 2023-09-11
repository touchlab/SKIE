package co.touchlab.skie.phases.debug.air.type

import co.touchlab.skie.phases.debug.air.element.AirConstantObject
import co.touchlab.skie.phases.debug.air.element.AirTypeAlias
import kotlinx.serialization.Serializable

@Serializable
data class AirTypeAbbreviation(
    val typeAlias: AirTypeAlias.Symbol,
    val hasQuestionMark: Boolean,
    val arguments: List<AirTypeArgument>,
    val annotations: List<AirConstantObject>,
)
