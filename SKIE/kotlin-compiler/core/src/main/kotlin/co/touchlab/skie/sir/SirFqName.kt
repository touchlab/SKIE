package co.touchlab.skie.sir

import co.touchlab.skie.sir.element.SirModule
import io.outfoxx.swiftpoet.DeclaredTypeName

class SirFqName private constructor(
    val module: SirModule,
    val simpleName: String,
    val parent: SirFqName? = null,
) {

    fun nested(name: String): SirFqName =
        SirFqName(module, name, this)

    fun toLocalString(): String =
        parent?.toLocalString()?.let { "$it.$simpleName" } ?: simpleName

    override fun toString(): String =
        module.name + "." + toLocalString()

    fun toSwiftPoetDeclaredTypeName(): DeclaredTypeName =
        parent?.toSwiftPoetDeclaredTypeName()?.nestedType(simpleName)
            ?: DeclaredTypeName.qualifiedTypeName(module.name + "." + simpleName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SirFqName

        if (module != other.module) return false
        if (simpleName != other.simpleName) return false
        if (parent != other.parent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = module.hashCode()
        result = 31 * result + simpleName.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        return result
    }

    companion object {

        operator fun invoke(
            module: SirModule,
            simpleName: String,
        ): SirFqName {
            val nameComponents = simpleName.split('.')

            return if (nameComponents.size == 1) {
                SirFqName(module, simpleName)
            } else {
                val parentName = nameComponents.dropLast(1).joinToString(".")

                SirFqName(module, parentName).nested(nameComponents.last())
            }
        }
    }
}
