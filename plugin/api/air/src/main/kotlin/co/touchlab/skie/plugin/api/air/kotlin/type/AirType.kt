package co.touchlab.skie.plugin.api.air.kotlin.type

import co.touchlab.skie.plugin.api.air.kotlin.element.AirConstantObject
import co.touchlab.skie.plugin.api.air.kotlin.element.AirTypeDeclaration
import kotlinx.serialization.Serializable

@Serializable
data class AirType(
    val declarationSymbol: AirTypeDeclaration.Symbol,
    val isNullable: Boolean,
    val arguments: List<AirTypeArgument>,
    val annotations: List<AirConstantObject>,
)
