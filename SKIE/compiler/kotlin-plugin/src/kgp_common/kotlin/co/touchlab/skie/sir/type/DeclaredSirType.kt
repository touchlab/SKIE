package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.util.swift.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.parameterizedBy

data class DeclaredSirType(
    val declaration: SirTypeDeclaration,
    val typeArguments: List<SirType> = emptyList(),
    val pointsToInternalName: Boolean = true
) : NonNullSirType() {

    override val isHashable: Boolean
        get() = declaration.isHashable

    override val isPrimitive: Boolean
        get() = declaration.isPrimitive

    fun withFqName(): DeclaredSirType =
        copy(pointsToInternalName = false)

    override val directlyReferencedTypes: List<SirType> = typeArguments

    fun toSwiftPoetDeclaredTypeName(): DeclaredTypeName =
        if (pointsToInternalName) declaration.internalName.toSwiftPoetName() else declaration.fqName.toExternalSwiftPoetName()

    override fun toSwiftPoetTypeName(): TypeName {
        val baseName = toSwiftPoetDeclaredTypeName()

        return if (typeArguments.isEmpty()) {
            baseName
        } else {
            baseName.parameterizedBy(typeArguments.map { it.toSwiftPoetTypeName() })
        }
    }
}

private fun SirFqName.toSwiftPoetName(): DeclaredTypeName =
    parent?.toSwiftPoetName()?.nestedType(simpleName)
        ?: if (module is SirModule.External) {
            DeclaredTypeName.qualifiedTypeName(module.name + "." + simpleName)
        } else {
            DeclaredTypeName.qualifiedLocalTypeName(simpleName)
        }

private fun SirFqName.toExternalSwiftPoetName(): DeclaredTypeName =
    parent?.toExternalSwiftPoetName()?.nestedType(simpleName)
        ?: DeclaredTypeName.qualifiedTypeName(module.name + "." + simpleName)
