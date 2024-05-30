package co.touchlab.skie.sir.type

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirTypeParameter
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.minimumVisibility
import co.touchlab.skie.sir.element.module
import co.touchlab.skie.sir.element.resolveAsKirClass
import io.outfoxx.swiftpoet.ParameterizedTypeName
import io.outfoxx.swiftpoet.parameterizedBy

class SirDeclaredSirType(
    private val declarationProvider: () -> SirTypeDeclaration,
    val typeArguments: List<SirType> = emptyList(),
) : DeclaredSirType() {

    val declaration: SirTypeDeclaration
        get() = declarationProvider()

    override val isHashable: Boolean
        get() = declaration.isHashable

    override val isReference: Boolean
        get() = declaration.isReference

    override fun asHashableType(): SirType? =
        this.takeIf { declaration.isHashable }

    override fun asReferenceType(): SirType? =
        this.takeIf { declaration.isReference }

    override fun evaluate(): EvaluatedSirType {
        val evaluatedTypeArguments = lazy { typeArguments.map { it.evaluate() } }

        val evaluatedType = lazy {
            if (declaration.module !is SirModule.Unknown) {
                SirDeclaredSirType(declarationProvider, typeArguments = evaluatedTypeArguments.value.map { it.type })
            } else {
                getUnknownCInteropModuleType()
            }
        }

        return EvaluatedSirType.Lazy(
            typeProvider = evaluatedType,
            canonicalNameProvider = lazy {
                val evaluatedTypeValue = evaluatedType.value

                if (evaluatedTypeValue is SirDeclaredSirType) {
                    getCanonicalName(evaluatedTypeValue, evaluatedTypeArguments.value)
                } else {
                    evaluatedType.value.evaluate().canonicalName
                }
            },
            swiftPoetTypeNameProvider = lazy {
                val evaluatedTypeValue = evaluatedType.value

                if (evaluatedTypeValue is SirDeclaredSirType) {
                    getSwiftPoetTypeName(evaluatedTypeValue.declaration, evaluatedTypeArguments.value)
                } else {
                    evaluatedType.value.evaluate().swiftPoetTypeName
                }
            },
            lowestVisibility = lazy {
                if (evaluatedType.value is SirDeclaredSirType) {
                    getVisibilityConstraint(evaluatedTypeArguments.value)
                } else {
                    evaluatedType.value.evaluate().visibilityConstraint
                }
            },
            referencedTypeDeclarationsProvider = lazy {
                if (evaluatedType.value is SirDeclaredSirType) {
                    getReferencedTypeDeclarations(evaluatedTypeArguments.value)
                } else {
                    setOf(declaration)
                }
            },
        )
    }

    private fun getUnknownCInteropModuleType(): SkieErrorSirType.UnknownCInteropFramework {
        val name = declaration.resolveAsKirClass()?.kotlinFqName ?: declaration.fqName.toLocalString()

        return SkieErrorSirType.UnknownCInteropFramework(name)
    }

    override fun inlineTypeAliases(): SirType {
        val inlinedTypeArguments = typeArguments.map { it.inlineTypeAliases() }

        return when (val declaration = declaration) {
            is SirClass -> SirDeclaredSirType(declarationProvider, typeArguments = inlinedTypeArguments)
            is SirTypeAlias -> {
                val substitutions = declaration.typeParameters.zip(inlinedTypeArguments).toMap()

                declaration.type.substituteTypeArguments(substitutions).inlineTypeAliases()
            }
        }
    }

    private fun getCanonicalName(evaluatedType: SirDeclaredSirType, evaluatedTypeArguments: List<EvaluatedSirType>): String =
        when (val declaration = evaluatedType.declaration) {
            is SirClass -> getCanonicalName(declaration, evaluatedTypeArguments)
            is SirTypeAlias -> evaluatedType.normalizedEvaluatedType().canonicalName
        }

    private fun getCanonicalName(sirClass: SirClass, evaluatedTypeArguments: List<EvaluatedSirType>): String {
        val usedTypeArgumentsCount = sirClass.typeParameters.size

        val remainingTypeArguments = evaluatedTypeArguments.dropLast(usedTypeArgumentsCount)

        val usedTypeArguments = evaluatedTypeArguments.takeLast(usedTypeArgumentsCount)

        val typeArgumentSuffix = if (evaluatedTypeArguments.isEmpty()) {
            ""
        } else {
            "<${usedTypeArguments.joinToString { it.canonicalName }}>"
        }

        val baseNameComponent = sirClass.namespace?.classDeclaration?.let { getCanonicalName(it, remainingTypeArguments) }

        return if (baseNameComponent != null) {
            baseNameComponent + "." + sirClass.simpleName + typeArgumentSuffix
        } else {
            sirClass.fqName.toString() + typeArgumentSuffix
        }
    }

    private fun getSwiftPoetTypeName(
        sirTypeDeclaration: SirTypeDeclaration,
        evaluatedTypeArguments: List<EvaluatedSirType>,
    ): ParameterizedTypeName {
        val usedTypeArgumentsCount = sirTypeDeclaration.typeParameters.size

        val remainingTypeArguments = evaluatedTypeArguments.dropLast(usedTypeArgumentsCount)

        val usedTypeArguments = evaluatedTypeArguments.takeLast(usedTypeArgumentsCount).map { it.swiftPoetTypeName }

        val baseNameComponent = sirTypeDeclaration.namespace?.classDeclaration?.let { getSwiftPoetTypeName(it, remainingTypeArguments) }

        return baseNameComponent?.nestedType(sirTypeDeclaration.simpleName, usedTypeArguments)
            ?: sirTypeDeclaration.fqName.toSwiftPoetDeclaredTypeName().parameterizedBy(usedTypeArguments)
    }

    private fun getVisibilityConstraint(evaluatedTypeArguments: List<EvaluatedSirType>): SirVisibility =
        (evaluatedTypeArguments.map { it.visibilityConstraint } + declaration.visibility).minimumVisibility()

    private fun getReferencedTypeDeclarations(evaluatedTypeArguments: List<EvaluatedSirType>): Set<SirTypeDeclaration> =
        evaluatedTypeArguments.flatMap { it.referencedTypeDeclarations }.toSet() + declaration

    override fun substituteTypeParameters(substitutions: Map<SirTypeParameter, SirTypeParameter>): SirDeclaredSirType =
        SirDeclaredSirType(declarationProvider, typeArguments = typeArguments.map { it.substituteTypeParameters(substitutions) })

    override fun substituteTypeArguments(substitutions: Map<SirTypeParameter, SirType>): SirDeclaredSirType =
        SirDeclaredSirType(declarationProvider, typeArguments = typeArguments.map { it.substituteTypeArguments(substitutions) })

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SirDeclaredSirType) return false

        if (typeArguments != other.typeArguments) return false
        if (declaration != other.declaration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = typeArguments.hashCode()
        result = 31 * result + declaration.hashCode()
        return result
    }

    override fun toString(): String {
        return "SirDeclaredSirType(declaration=$declaration, typeArguments=$typeArguments)"
    }
}
