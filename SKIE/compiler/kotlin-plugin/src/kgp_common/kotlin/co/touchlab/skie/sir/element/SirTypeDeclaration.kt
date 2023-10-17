package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.SirType

sealed interface SirTypeDeclaration : SirDeclaration {

    /**
     * Last component of fqName.
     */
    var simpleName: String

    var visibility: SirVisibility

    override var parent: SirDeclarationParent

    /**
     * Base component of fqName.
     */
    var namespace: SirDeclarationNamespace?
        get() = parent as? SirDeclarationNamespace
        set(value) {
            parent = value ?: namespaceParent
        }

    /**
     * Name used to generate SKIE code.
     */
    val fqName: SirFqName
        get() = namespace?.fqName?.nested(simpleName) ?: SirFqName(module, simpleName)

    val originalFqName: SirFqName

    /**
     * Name that is expected to be used by external Swift code.
     */
    val publicName: SirFqName

    /**
     * Name used by SKIE generated code to avoid many problems with ambiguous identifiers and bugs in Swift compiler.
     */
    val internalName: SirFqName

    val typeParameters: List<SirTypeParameter>

    val isHashable: Boolean

    val isPrimitive: Boolean

    val defaultType: DeclaredSirType

    fun toType(typeArguments: List<SirType>): DeclaredSirType =
        DeclaredSirType(this, typeArguments = typeArguments)

    fun toType(vararg typeArguments: SirType): DeclaredSirType =
        toType(typeArguments.toList())
}

fun SirTypeDeclaration.toTypeFromEnclosingTypeParameters(typeParameters: List<SirTypeParameter>): DeclaredSirType =
    toType(typeParameters.map { it.toTypeParameterUsage() })

