package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.kirClassOrNull
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirDeclaredSirType

class SirClass(
    override var baseName: String,
    parent: SirDeclarationParent,
    // Class requires explicit declaration of inheritance from AnyObject
    var kind: Kind,
    override var visibility: SirVisibility = SirVisibility.Public,
    override var isReplaced: Boolean = false,
    superTypes: List<SirDeclaredSirType> = emptyList(),
    attributes: List<String> = emptyList(),
    var publicTypeAlias: SirTypeAlias? = null,
    var internalTypeAlias: SirTypeAlias? = null,
    var isInherentlyHashable: Boolean = false,
    var isAlwaysAReference: Boolean = false,
    val origin: Origin = Origin.Generated,
) : SirTypeDeclaration, SirDeclarationNamespace, SirTypeParameterParent, SirElementWithAttributes {

    // TODO If modality is added update [SirHierarchyCache.canTheoreticallyInheritFrom]

    override val classDeclaration: SirClass
        get() = this

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    val superTypes: MutableList<SirDeclaredSirType> = superTypes.toMutableList()

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val defaultType: SirDeclaredSirType by lazy {
        toType(emptyList())
    }

    override val isHashable: Boolean
        get() = isInherentlyHashable || superTypes.any { it.isHashable }

    override val isReference: Boolean
        get() = isAlwaysAReference || superTypes.any { it.isReference }

    override val typeParameters: MutableList<SirTypeParameter> = mutableListOf()

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    val enumCases: MutableList<SirEnumCase> = mutableListOf()

    /**
     * Actual fully qualified name (including module) of the declaration. Used by SKIE to generate code if possible.
     */
    override val fqName: SirFqName
        get() = super.fqName

    /**
     * Name that is expected to be used by external Swift code. Used primarily for comments, logs, etc.
     */
    override val publicName: SirFqName
        get() = publicTypeAlias?.publicName ?: fqName

    /**
     * Name used by SKIE generated code in cases it cannot use fqName.
     */
    override val internalName: SirFqName
        get() = internalTypeAlias?.internalName ?: publicName

    override fun toReadableString(): String =
        kind.toString().lowercase() + " " + fqName.toString()

    override fun toString(): String = "${this::class.simpleName}: $fqName${if (fqName != publicName) "($publicName)" else ""}"

    sealed interface Origin {

        object ExternalSwiftFramework : Origin

        object Generated : Origin

        data class Oir(val oirClass: OirClass) : Origin

        data class Kir(val kirClass: KirClass) : Origin
    }

    enum class Kind {
        Class,
        Enum,
        Struct,
        Protocol;

        val isClass: Boolean
            get() = this == Class

        val isEnum: Boolean
            get() = this == Enum

        val isStruct: Boolean
            get() = this == Struct

        val isProtocol: Boolean
            get() = this == Protocol
    }

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            baseName: String,
            kind: Kind = Kind.Class,
            visibility: SirVisibility = SirVisibility.Public,
            isReplaced: Boolean = false,
            superTypes: List<SirDeclaredSirType> = emptyList(),
            attributes: List<String> = emptyList(),
            publicTypeAlias: SirTypeAlias? = null,
            internalTypeAlias: SirTypeAlias? = null,
            isInherentlyHashable: Boolean = false,
            origin: Origin = Origin.Generated,
        ): SirClass =
            SirClass(
                baseName = baseName,
                parent = this@SirDeclarationParent,
                kind = kind,
                visibility = visibility,
                isReplaced = isReplaced,
                superTypes = superTypes,
                attributes = attributes,
                publicTypeAlias = publicTypeAlias,
                internalTypeAlias = internalTypeAlias,
                isInherentlyHashable = isInherentlyHashable,
                origin = origin,
            )
    }
}

fun OirClass.Kind.toSirKind(): SirClass.Kind =
    when (this) {
        OirClass.Kind.Class -> SirClass.Kind.Class
        OirClass.Kind.Protocol -> SirClass.Kind.Protocol
    }

val SirClass.superClassType: SirDeclaredSirType?
    get() = superTypes.map { it.resolveAsSirClassType() }
        .firstOrNull { (it?.declaration as? SirClass)?.kind == SirClass.Kind.Class }

val SirClass.superClass: SirClass?
    get() = superClassType?.declaration as? SirClass

fun SirDeclaredSirType.resolveAsSirClassType(): SirDeclaredSirType? =
    when (declaration) {
        is SirClass -> this
        is SirTypeAlias -> {
            when (val type = declaration.type) {
                is SirDeclaredSirType -> type.resolveAsSirClassType()
                else -> null
            }
        }
    }

fun SirDeclaredSirType.resolveAsSirClass(): SirClass? =
    resolveAsSirClassType()?.declaration as? SirClass

val SirClass.oirClassOrNull: OirClass?
    get() = when (origin) {
        is SirClass.Origin.Kir -> origin.kirClass.oirClass
        is SirClass.Origin.Oir -> origin.oirClass
        SirClass.Origin.Generated -> null
        SirClass.Origin.ExternalSwiftFramework -> null
    }

val SirClass.kirClassOrNull: KirClass?
    get() = oirClassOrNull?.kirClassOrNull
