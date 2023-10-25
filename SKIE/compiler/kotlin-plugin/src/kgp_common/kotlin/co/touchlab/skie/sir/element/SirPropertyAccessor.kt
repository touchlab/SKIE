package co.touchlab.skie.sir.element

import io.outfoxx.swiftpoet.FunctionSpec

sealed interface SirPropertyAccessor : SirElementWithAttributes, SirElementWithSwiftPoetBuilderModifications<FunctionSpec.Builder> {

    var property: SirProperty
}
