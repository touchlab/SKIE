package co.touchlab.skie.sir.element

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.kirClassOrNull
import co.touchlab.skie.sir.SirFqName
import co.touchlab.skie.sir.element.util.sirDeclarationParent
import co.touchlab.skie.sir.type.SirDeclaredSirType
import co.touchlab.skie.sir.type.SirType
import io.outfoxx.swiftpoet.FunctionSpec
import org.intellij.lang.annotations.Language

class SirClass(
    override var baseName: String,
    parent: SirDeclarationParent,
    // Class requires explicit declaration of inheritance from AnyObject
    var kind: Kind,
    override var modality: SirModality = coerceModalityForClass(kind),
    override var visibility: SirVisibility = SirVisibility.Public,
    override var isReplaced: Boolean = false,
    override var isHidden: Boolean = false,
    superTypes: List<SirDeclaredSirType> = emptyList(),
    attributes: List<String> = emptyList(),
    var publicTypeAlias: SirTypeAlias? = null,
    var internalTypeAlias: SirTypeAlias? = null,
    var isInherentlyHashable: Boolean = false,
    var isAlwaysAReference: Boolean = false,
    val origin: Origin = Origin.Generated,
) : SirTypeDeclaration,
    SirDeclarationNamespace,
    SirDeclarationWithSuperTypes,
    SirTypeParameterParent,
    SirElementWithAttributes,
    SirElementWithModality,
    SirConditionalConstraintParent {

    // TODO If modality is added update [SirHierarchyCache.canTheoreticallyInheritFrom]

    override val classDeclaration: SirClass
        get() = this

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    @Language("markdown")
    override var documentation: String = ""

    override val superTypes: MutableList<SirDeclaredSirType> = superTypes.toMutableList()

    override val attributes: MutableList<String> = attributes.toMutableList()

    override val conditionalConstraints: MutableList<SirConditionalConstraint> = mutableListOf()

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

    val deinitBuilder = mutableListOf<FunctionSpec.Builder.() -> Unit>()

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
        get() = internalTypeAlias?.internalName ?: fqName

    override fun toType(typeArguments: List<SirType>): SirDeclaredSirType =
        SirDeclaredSirType({ internalTypeAlias ?: this }, typeArguments = typeArguments)

    override fun toReadableString(): String = kind.toString().lowercase() + " " + fqName.toString()

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
        Protocol,
        Actor,
        ;

        val isClass: Boolean
            get() = this == Class

        val isEnum: Boolean
            get() = this == Enum

        val isStruct: Boolean
            get() = this == Struct

        val isProtocol: Boolean
            get() = this == Protocol

        val isActor: Boolean
            get() = this == Actor
    }

    companion object {

        context(SirDeclarationParent)
        operator fun invoke(
            baseName: String,
            kind: Kind = Kind.Class,
            visibility: SirVisibility = SirVisibility.Public,
            modality: SirModality = coerceModalityForClass(kind),
            isReplaced: Boolean = false,
            isHidden: Boolean = false,
            superTypes: List<SirDeclaredSirType> = emptyList(),
            attributes: List<String> = emptyList(),
            publicTypeAlias: SirTypeAlias? = null,
            internalTypeAlias: SirTypeAlias? = null,
            isInherentlyHashable: Boolean = false,
            origin: Origin = Origin.Generated,
        ): SirClass = SirClass(
            baseName = baseName,
            parent = this@SirDeclarationParent,
            kind = kind,
            modality = modality,
            visibility = visibility,
            isReplaced = isReplaced,
            isHidden = isHidden,
            superTypes = superTypes,
            attributes = attributes,
            publicTypeAlias = publicTypeAlias,
            internalTypeAlias = internalTypeAlias,
            isInherentlyHashable = isInherentlyHashable,
            origin = origin,
        )
    }
}

fun OirClass.Kind.toSirKind(): SirClass.Kind = when (this) {
    OirClass.Kind.Class -> SirClass.Kind.Class
    OirClass.Kind.Protocol -> SirClass.Kind.Protocol
}

val SirClass.superClassType: SirDeclaredSirType?
    get() = superTypes.map { it.resolveAsSirClassType() }
        .firstOrNull { (it?.declaration as? SirClass)?.kind == SirClass.Kind.Class }

val SirClass.superProtocolTypes: List<SirDeclaredSirType>
    get() = superTypes.mapNotNull { it.resolveAsSirClassType() }
        .filter { (it.declaration as? SirClass)?.kind == SirClass.Kind.Protocol }

val SirClass.superClass: SirClass?
    get() = superClassType?.declaration as? SirClass

fun SirDeclaredSirType.resolveAsSirClassType(): SirDeclaredSirType? = when (val declaration = declaration) {
    is SirClass -> this
    is SirTypeAlias -> {
        when (val type = declaration.type) {
            is SirDeclaredSirType -> type.resolveAsSirClassType()
            else -> null
        }
    }
}

fun SirDeclaredSirType.resolveAsSirClass(): SirClass? = resolveAsSirClassType()?.declaration as? SirClass

val SirClass.oirClassOrNull: OirClass?
    get() = when (origin) {
        is SirClass.Origin.Kir -> origin.kirClass.oirClass
        is SirClass.Origin.Oir -> origin.oirClass
        SirClass.Origin.Generated -> null
        SirClass.Origin.ExternalSwiftFramework -> null
    }

val SirClass.kirClassOrNull: KirClass?
    get() = oirClassOrNull?.kirClassOrNull

fun coerceModalityForClass(kind: SirClass.Kind): SirModality = when (kind) {
    SirClass.Kind.Class, SirClass.Kind.Actor -> SirModality.ModuleLimited
    SirClass.Kind.Enum, SirClass.Kind.Struct -> SirModality.Final
    SirClass.Kind.Protocol -> SirModality.Open
}
