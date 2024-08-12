package co.touchlab.skie.phases.runtime.declarations

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.element.SirTypeParameter

data class SkieSwiftFlowWithInitialValue(
    val self: SirClass,
    val flowProperty: SirProperty,
    val initialValueProperty: SirProperty,
    val elementTypeParameter: SirTypeParameter,
)
