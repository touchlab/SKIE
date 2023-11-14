package co.touchlab.skie.kir.element

sealed interface DeprecationLevel {

    object None : DeprecationLevel

    data class Warning(val message: String?) : DeprecationLevel

    data class Error(val message: String?) : DeprecationLevel
}
