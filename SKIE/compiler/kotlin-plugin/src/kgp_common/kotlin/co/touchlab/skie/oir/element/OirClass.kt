package co.touchlab.skie.oir.element

import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.sir.element.SirClass

class OirClass(
    override val name: String,
    override val parent: OirTopLevelDeclarationParent,
    val kind: Kind,
) : OirTypeDeclaration, OirCallableDeclarationParent {

    lateinit var originalSirClass: SirClass

    val primarySirClass: SirClass
        get() = bridgedSirClass ?: originalSirClass

    var bridgedSirClass: SirClass? = null

    override val visibility: OirVisibility
        get() = originalSirClass.visibility.toOirVisibility()

    val superTypes: MutableList<DeclaredOirType> = mutableListOf()

    val extensions: MutableList<OirExtension> = mutableListOf()

    override val callableDeclarations: MutableList<OirCallableDeclaration> = mutableListOf()

    val callableDeclarationsIncludingExtensions: List<OirCallableDeclaration>
        get() = callableDeclarations + extensions.flatMap { it.callableDeclarations }

    val typeParameters: MutableList<OirTypeParameter> = mutableListOf()

    init {
        parent.declarations.add(this)
    }

    override val defaultType: DeclaredOirType by lazy {
        toType(emptyList())
    }

    override fun toString(): String =
        "${this::class.simpleName}: $name"

    enum class Kind {
        Class,
        Protocol,
    }
}

val OirClass.superClassType: DeclaredOirType?
    get() = superTypes.map { it.resolveAsOirClassType() }
        .firstOrNull { (it?.declaration as? OirClass)?.kind == OirClass.Kind.Class }

val OirClass.superClass: OirClass?
    get() = superClassType?.declaration as? OirClass

fun DeclaredOirType.resolveAsOirClassType(): DeclaredOirType? =
    when (declaration) {
        is OirClass -> this
        is OirTypeDef -> {
            when (val type = declaration.type) {
                is DeclaredOirType -> type.resolveAsOirClassType()
                else -> null
            }
        }
    }

fun OirClass.renderForwardDeclaration(): String =
    if (typeParameters.isEmpty()) name else "$name<${typeParameters.joinToString(", ") { it.name }}>"

val OirClass.functions: List<OirFunction>
    get() = callableDeclarations.filterIsInstance<OirFunction>()

val OirClass.constructors: List<OirConstructor>
    get() = callableDeclarations.filterIsInstance<OirConstructor>()

val OirClass.simpleFunctions: List<OirSimpleFunction>
    get() = callableDeclarations.filterIsInstance<OirSimpleFunction>()
