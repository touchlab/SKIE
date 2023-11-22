package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.type.DeclaredSirType
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType

sealed interface SirTypeDeclaration : SirDeclaration {

    /**
     * Used to derive other names.
     */
    var baseName: String

    /**
     * Use `simpleName` in generated Swift code.
     */
    val simpleName: String
        get() = when (visibility) {
            SirVisibility.PublicButReplaced -> "__$baseName"
            else -> baseName
        }

    override var parent: SirDeclarationParent

    /**
     * Base component of fqName.
     */
    var namespace: SirDeclarationNamespace?
        get() = parent as? SirDeclarationNamespace
        set(value) {
            parent = value ?: firstParentThatIsNotNamespace
        }

    /**
     * Name used to generate SKIE code.
     */
    val fqName: SirFqName
        get() = namespace?.fqName?.nested(simpleName) ?: SirFqName(module, simpleName)

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

    val isReference: Boolean

    val defaultType: DeclaredSirType

    fun toType(typeArguments: List<SirType>): SirDeclaredSirType =
        SirDeclaredSirType(this, typeArguments = typeArguments)

    fun toType(vararg typeArguments: SirType): SirDeclaredSirType =
        toType(typeArguments.toList())
}

fun SirTypeDeclaration.toTypeFromEnclosingTypeParameters(typeParameters: List<SirTypeParameter>): DeclaredSirType =
    toType(typeParameters.map { it.toTypeParameterUsage() })

fun SirTypeDeclaration.resolveAsSirClass(): SirClass? =
    when (this) {
        is SirClass -> this
        is SirTypeAlias -> {
            when (val type = type) {
                is SirTypeDeclaration -> type.resolveAsSirClass()
                else -> null
            }
        }
    }

fun SirTypeDeclaration.resolveAsKirClass(): KirClass? =
    resolveAsSirClass()?.kirClassOrNull
