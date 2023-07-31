package co.touchlab.skie.debug.air.type

import kotlinx.serialization.Serializable

@Serializable
sealed interface AirTypeArgument {

    @Serializable
    object StarProjection : AirTypeArgument

    @Serializable
    data class TypeProjection(
        val variance: AirTypeVariance,
        val type: AirType,
    ) : AirTypeArgument
}
