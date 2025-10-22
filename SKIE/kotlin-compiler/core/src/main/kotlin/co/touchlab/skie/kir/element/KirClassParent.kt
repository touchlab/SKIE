package co.touchlab.skie.kir.element

sealed interface KirClassParent : KirElement {

    val module: KirModule

    val classes: MutableList<KirClass>
}
