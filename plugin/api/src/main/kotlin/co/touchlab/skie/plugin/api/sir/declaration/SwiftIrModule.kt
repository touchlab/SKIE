package co.touchlab.skie.plugin.api.sir.declaration

class SwiftIrModule(
    val name: String,
): SwiftIrDeclaration {

    override fun toString(): String = "module: $name"
}
