package co.touchlab.skie.phases.runtime.declarations

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirTypeParameter

data class Observing(
    val self: SirClass,
    val valuesTypeParameter: SirTypeParameter,
    val initialContentTypeParameter: SirTypeParameter,
    val contentTypeParameter: SirTypeParameter,
)
