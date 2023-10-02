package co.touchlab.skie.sir.element

interface SirOverridableDeclaration<T : SirOverridableDeclaration<T>> {

    val memberOwner: SirClass?

    val overriddenDeclarations: MutableList<T>
}
