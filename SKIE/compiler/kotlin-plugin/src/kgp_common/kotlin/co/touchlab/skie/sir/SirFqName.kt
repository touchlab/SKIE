package co.touchlab.skie.sir

import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.util.swift.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName

// TODO Needs to support type parameters for types nested in generic classes (DeclaredSirType also needs to support this)
data class SirFqName(
    val module: SirModule,
    val simpleName: String,
    val parent: SirFqName? = null,
) {

    fun nested(name: String): SirFqName =
        SirFqName(module, name, this)

    // WIP Should be used only from DeclaredSirType
    // WIP Then remove DeclaredSirType.toInternalSwiftPoetName
    fun toSwiftPoetName(): DeclaredTypeName =
        parent?.toSwiftPoetName()?.nestedType(simpleName)
            ?: if (module is SirModule.External) {
                DeclaredTypeName.qualifiedTypeName(module.name + "." + simpleName)
            } else {
                DeclaredTypeName.qualifiedLocalTypeName(simpleName)
            }

    fun toExternalSwiftPoetName(): DeclaredTypeName =
        parent?.toExternalSwiftPoetName()?.nestedType(simpleName)
            ?: DeclaredTypeName.qualifiedTypeName(module.name + "." + simpleName)

    fun toLocalUnescapedNameString(): String =
        parent?.toLocalUnescapedNameString()?.let { "$it.$simpleName" } ?: simpleName

    override fun toString(): String =
        toSwiftPoetName().toString()
}
