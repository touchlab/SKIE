package co.touchlab.skie.sir.element

// All subclasses can be instantiated only in SirFileProvider or SirModule
sealed interface SirFile : SirElement {

    val module: SirModule
}
