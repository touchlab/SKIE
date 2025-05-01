package co.touchlab.skie.sir.element

sealed interface SirPropertyAccessor :
    SirElementWithAttributes,
    SirElementWithFunctionBodyBuilder {

    var property: SirProperty
}
