package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.RootConfiguration

class KirProject(
    val configuration: RootConfiguration,
) : KirElement {

    val modules: MutableList<KirModule> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}"
}
