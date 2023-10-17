package co.touchlab.skie.sir.element

import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.DeclaredSirType

class SirClass(
    override var simpleName: String,
    parent: SirDeclarationParent,
    var kind: Kind = Kind.Class,
    override var visibility: SirVisibility = SirVisibility.Public,
    superTypes: List<DeclaredSirType> = emptyList(),
    attributes: List<String> = emptyList(),
    var publicTypeAlias: SirTypeAlias? = null,
    var internalTypeAlias: SirTypeAlias? = null,
    var isInherentlyHashable: Boolean = false,
    override var isPrimitive: Boolean = false,
) : SirTypeDeclaration, SirDeclarationNamespace, SirTypeParameterParent, SirElementWithAttributes {

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    val superTypes: MutableList<DeclaredSirType> = superTypes.toMutableList()

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val defaultType: DeclaredSirType by lazy {
        toType(emptyList())
    }

    override val isHashable: Boolean
        get() = isInherentlyHashable || superTypes.any { it.isHashable }

    override val typeParameters: MutableList<SirTypeParameter> = mutableListOf()

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    /**
     * Name used to generate SKIE code.
     */
    override val fqName: SirFqName
        get() = super.fqName

    override val originalFqName: SirFqName = fqName

    /**
     * Name that is expected to be used by external Swift code.
     */
    override val publicName: SirFqName
        get() = publicTypeAlias?.publicName ?: fqName

    /**
     * Name used by SKIE generated code to avoid many problems with ambiguous identifiers and bugs in Swift compiler.
     */
    override val internalName: SirFqName
        get() = internalTypeAlias?.internalName ?: publicName

    override fun toString(): String = "${this::class.simpleName}: $fqName${if (fqName != publicName) "($publicName)" else ""}"

    enum class Kind {
        Class,
        Enum,
        Struct,
        Protocol,
    }

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            simpleName: String,
            kind: Kind = Kind.Class,
            visibility: SirVisibility = SirVisibility.Public,
            superTypes: List<DeclaredSirType> = emptyList(),
            attributes: List<String> = emptyList(),
            internalTypeAlias: SirTypeAlias? = null,
            isInherentlyHashable: Boolean = false,
            isPrimitive: Boolean = false,
        ): SirClass =
            SirClass(
                simpleName = simpleName,
                parent = this@SirDeclarationParent,
                kind = kind,
                visibility = visibility,
                superTypes = superTypes,
                attributes = attributes,
                internalTypeAlias = internalTypeAlias,
                isInherentlyHashable = isInherentlyHashable,
                isPrimitive = isPrimitive,
            )
    }
}

val SirClass.superClassType: DeclaredSirType?
    get() = superTypes.map { it.resolveAsDirectClassSirType() }
        .firstOrNull { (it?.declaration as? SirClass)?.kind == SirClass.Kind.Class }

val SirClass.superClass: SirClass?
    get() = superClassType?.declaration as? SirClass

fun DeclaredSirType.resolveAsDirectClassSirType(): DeclaredSirType? =
    when (declaration) {
        is SirClass -> this
        is SirTypeAlias -> {
            when (val type = declaration.type) {
                is DeclaredSirType -> type.resolveAsDirectClassSirType()
                else -> null
            }
        }
    }

fun DeclaredSirType.resolveAsDirectSirClass(): SirClass? =
    resolveAsDirectClassSirType()?.declaration as? SirClass
