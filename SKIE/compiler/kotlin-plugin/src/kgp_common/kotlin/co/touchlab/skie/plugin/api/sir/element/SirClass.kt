package co.touchlab.skie.plugin.api.sir.element

import co.touchlab.skie.plugin.api.model.SwiftGenericExportScope
import co.touchlab.skie.plugin.api.sir.SirFqName
import co.touchlab.skie.plugin.api.sir.element.util.sirDeclarationParent
import co.touchlab.skie.plugin.api.sir.type.DeclaredSirType
import io.outfoxx.swiftpoet.TypeSpec

class SirClass(
    override var simpleName: String,
    parent: SirDeclarationParent,
    var kind: Kind = Kind.Class,
    override var visibility: SirVisibility = SirVisibility.Public,
    superTypes: List<DeclaredSirType> = emptyList(),
    var internalTypeAlias: SirTypeAlias? = null,
    var isInherentlyHashable: Boolean = false,
    override var isPrimitive: Boolean = false,
) : SirTypeDeclaration, SirDeclarationNamespace, SirTypeParameterParent {

    override var parent: SirDeclarationParent by sirDeclarationParent(parent)

    val superTypes: MutableList<DeclaredSirType> = superTypes.toMutableList()

    override val defaultType: DeclaredSirType by lazy {
        toType(emptyList())
    }

    override val isHashable: Boolean
        get() = isInherentlyHashable || superTypes.any { it.isHashable }

    override val typeParameters: MutableList<SirTypeParameter> = mutableListOf()

    override val declarations: MutableList<SirDeclaration> = mutableListOf()

    override val fqName: SirFqName
        get() = super.fqName

    override val originalFqName: SirFqName = fqName

    /**
     * Name used by SKIE generated code to avoid many problems with ambiguous identifiers and bugs in Swift compiler.
     */
    override val internalName: SirFqName
        get() = internalTypeAlias?.fqName ?: fqName

    var bridgingTypeAlias: SirTypeAlias? = null

    /**
     * Name used by SKIE in ApiNotes to configure bridging to avoid problem with ApiNotes not being able to reference nested classes.
     */
    val bridgingName: SirFqName
        get() = bridgingTypeAlias?.fqName ?: fqName

    // TODO Replace SwiftPoet with Sir
    val swiftPoetBuilderModifications = mutableListOf<TypeSpec.Builder.() -> Unit>()

    override fun toString(): String =
        "${this::class.simpleName}: $fqName"

    enum class Kind {
        Class,
        Enum,
        Struct,
        Protocol,
    }
}

val SirClass.superClassType: DeclaredSirType?
    get() = superTypes.map { it.resolveAsDirectClassSirType() }
        .firstOrNull { (it?.declaration as? SirClass)?.kind == SirClass.Kind.Class }

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

