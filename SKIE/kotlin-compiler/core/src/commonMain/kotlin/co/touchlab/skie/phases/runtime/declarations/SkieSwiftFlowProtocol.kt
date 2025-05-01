package co.touchlab.skie.phases.runtime.declarations

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirProperty

data class SkieSwiftFlowProtocol(val self: SirClass, val delegateProperty: SirProperty)
