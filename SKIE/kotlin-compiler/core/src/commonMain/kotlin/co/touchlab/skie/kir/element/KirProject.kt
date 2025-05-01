package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.GlobalConfiguration

class KirProject(val configuration: GlobalConfiguration) : KirElement {

    val modules: MutableList<KirModule> = mutableListOf()

    override fun toString(): String = "${this::class.simpleName}"
}
