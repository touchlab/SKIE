package co.touchlab.skie.analytics.air.element

enum class AirModality {
    Final, Sealed, Open, Abstract
}

val AirModality.isFinal: Boolean
    get() = this == AirModality.Final

val AirModality.isSealed: Boolean
    get() = this == AirModality.Sealed

val AirModality.isOpen: Boolean
    get() = this == AirModality.Open

val AirModality.isAbstract: Boolean
    get() = this == AirModality.Abstract
