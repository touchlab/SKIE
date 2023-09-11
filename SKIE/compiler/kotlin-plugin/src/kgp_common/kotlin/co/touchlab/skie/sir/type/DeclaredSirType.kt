package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirTypeDeclaration
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.parameterizedBy

data class DeclaredSirType(
    val declaration: SirTypeDeclaration,
    val typeArguments: List<SirType> = emptyList(),
) : NonNullSirType() {

    override val isHashable: Boolean
        get() = declaration.isHashable

    override val isPrimitive: Boolean
        get() = declaration.isPrimitive

    var useInternalName: Boolean = true

    override val directlyReferencedTypes: List<SirType> = typeArguments

    override fun toSwiftPoetUsage(): TypeName {
        val baseName = if (useInternalName) declaration.internalName.toSwiftPoetName() else declaration.fqName.toExternalSwiftPoetName()

        return if (typeArguments.isEmpty()) {
            baseName
        } else {
            baseName.parameterizedBy(typeArguments.map { it.toSwiftPoetUsage() })
        }
    }
}
