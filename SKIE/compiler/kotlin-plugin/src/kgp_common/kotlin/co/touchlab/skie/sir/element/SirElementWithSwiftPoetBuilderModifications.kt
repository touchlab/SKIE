package co.touchlab.skie.sir.element

interface SirElementWithSwiftPoetBuilderModifications<BUILDER> {

    val swiftPoetBuilderModifications: MutableList<BUILDER.() -> Unit>
}
