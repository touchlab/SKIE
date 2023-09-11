package co.touchlab.skie.phases.debug.air.type

import co.touchlab.skie.phases.debug.air.element.AirConstantObject
import co.touchlab.skie.phases.debug.air.element.AirTypeDeclaration
import kotlinx.serialization.Serializable

@Serializable
data class AirType(
    val classifier: AirTypeDeclaration.Symbol,
    val isNullable: Boolean,
    val arguments: List<AirTypeArgument>,
    val annotations: List<AirConstantObject>,
    val abbreviation: AirTypeAbbreviation?,
)
