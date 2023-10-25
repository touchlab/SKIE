package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.util.swift.qualifiedLocalTypeName
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.parameterizedBy

data class SirDeclaredSirType(
    val declaration: SirTypeDeclaration,
    val typeArguments: List<SirType> = emptyList(),
    override val pointsToInternalName: Boolean = true,
) : DeclaredSirType() {

    override val isHashable: Boolean
        get() = declaration.isHashable

    override val isReference: Boolean
        get() = declaration.isReference

    override fun asHashableType(): SirType? =
        this.takeIf { declaration.isHashable }

    override fun asReferenceType(): SirType? =
        this.takeIf { declaration.isReference }

    override fun evaluate(): EvaluatedSirType<SirDeclaredSirType> {
        val evaluatedTypeArguments = typeArguments.map { it.evaluate() }

        return EvaluatedSirType(
            type = copy(typeArguments = evaluatedTypeArguments.map { it.type }),
            isValid = evaluatedTypeArguments.all { it.isValid },
            canonicalName = getCanonicalName(evaluatedTypeArguments),
            swiftPoetTypeName = getSwiftPoetTypeName(evaluatedTypeArguments),
        )
    }

    fun toSwiftPoetDeclaredTypeName(): DeclaredTypeName =
        if (pointsToInternalName) declaration.internalName.toSwiftPoetName() else declaration.fqName.toExternalSwiftPoetName()

    private fun getCanonicalName(evaluatedTypeArguments: List<EvaluatedSirType<SirType>>): String {
        val typeArgumentSuffix = if (evaluatedTypeArguments.isEmpty()) {
            ""
        } else {
            "<${evaluatedTypeArguments.joinToString { it.canonicalName }}>"
        }

        return when (declaration) {
            is SirClass -> declaration.fqName.toString() + typeArgumentSuffix
            is SirTypeAlias -> {
                val substitutions = declaration.typeParameters.zip(typeArguments).toMap()

                declaration.type.substituteTypeArguments(substitutions).evaluate().canonicalName + typeArgumentSuffix
            }
        }
    }

    private fun getSwiftPoetTypeName(evaluatedTypeArguments: List<EvaluatedSirType<SirType>>): TypeName {
        val baseName = toSwiftPoetDeclaredTypeName()

        return if (evaluatedTypeArguments.isEmpty()) {
            baseName
        } else {
            baseName.parameterizedBy(evaluatedTypeArguments.map { it.swiftPoetTypeName })
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

    override fun withFqName(): SirDeclaredSirType =
        copy(pointsToInternalName = false)

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): SirDeclaredSirType =
        copy(typeArguments = typeArguments.map { it.substituteTypeParameters(substitutions) })

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SirDeclaredSirType =
        copy(typeArguments = typeArguments.map { it.substituteTypeArguments(substitutions) })
}
