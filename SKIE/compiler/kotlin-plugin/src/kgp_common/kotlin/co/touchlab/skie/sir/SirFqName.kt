package co.touchlab.skie.sir

import co.touchlab.skie.sir.element.SirModule

// TODO Needs to support type parameters for types nested in generic classes (DeclaredSirType also needs to support this)
data class SirFqName(
    val module: SirModule,
    val simpleName: String,
    val parent: SirFqName? = null,
) {

    fun nested(name: String): SirFqName =
        SirFqName(module, name, this)

    fun toLocalString(): String =
        parent?.toLocalString()?.let { "$it.$simpleName" } ?: simpleName

    override fun toString(): String =
        if (module is SirModule.External) {
            module.name + "." + toLocalString()
        } else {
            toLocalString()
        }
}
