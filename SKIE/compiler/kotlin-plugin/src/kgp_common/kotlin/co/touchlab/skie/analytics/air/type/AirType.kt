package co.touchlab.skie.analytics.air.type

import co.touchlab.skie.analytics.air.element.AirConstantObject
import co.touchlab.skie.analytics.air.element.AirTypeDeclaration
import kotlinx.serialization.Serializable

@Serializable
data class AirType(
    val classifier: AirTypeDeclaration.Symbol,
    val isNullable: Boolean,
    val arguments: List<AirTypeArgument>,
    val annotations: List<AirConstantObject>,
    val abbreviation: AirTypeAbbreviation?,
)
