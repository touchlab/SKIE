package co.touchlab.skie.analytics.air.type

enum class AirTypeVariance {
    /** T */
    Invariant,

    /** out T */
    Covariant,

    /** in T */
    Contravariant,
}

val AirTypeVariance.isInvariant: Boolean
    get() = this == AirTypeVariance.Invariant

val AirTypeVariance.isCovariant: Boolean
    get() = this == AirTypeVariance.Covariant

val AirTypeVariance.isContravariant: Boolean
    get() = this == AirTypeVariance.Contravariant
