package co.touchlab.skie.oir.element

import co.touchlab.skie.sir.element.SirExtension

class OirExtension(
    val classDeclaration: OirClass,
    override val parent: OirTopLevelDeclarationParent,
) : OirTopLevelDeclaration, OirCallableDeclarationParent {

    lateinit var sirExtension: SirExtension

    override val callableDeclarations: MutableList<OirCallableDeclaration> = mutableListOf()

    override val visibility: OirVisibility
        get() = sirExtension.visibility.toOirVisibility()

    init {
        parent.declarations.add(this)
        classDeclaration.extensions.add(this)
    }

    override fun toString(): String =
        "${this::class.simpleName}: ${classDeclaration.name}"
}
