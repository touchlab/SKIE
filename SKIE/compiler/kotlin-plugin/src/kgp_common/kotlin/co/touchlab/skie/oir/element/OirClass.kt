package co.touchlab.skie.oir.element

import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.oir.type.DeclaredOirType
import co.touchlab.skie.oir.type.OirType
import co.touchlab.skie.sir.element.SirClass

class OirClass(
    override val name: String,
    override val parent: OirTopLevelDeclarationParent,
    val kind: Kind,
    val origin: Origin,
) : OirTypeDeclaration, OirCallableDeclarationParent {

    lateinit var originalSirClass: SirClass

    // Should not be directly accessed before all bridging configuration is done. See @MustBeExecutedAfterBridgingConfiguration.
    val primarySirClass: SirClass
        get() = bridgedSirClass ?: originalSirClass

    // Should not be directly accessed before all bridging configuration is done. See @MustBeExecutedAfterBridgingConfiguration.
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

    override fun toType(typeArguments: List<OirType>): DeclaredOirType =
        DeclaredOirType(this, typeArguments = typeArguments)

    override fun toType(vararg typeArguments: OirType): DeclaredOirType =
        toType(typeArguments.toList())

    override fun toString(): String =
        "${this::class.simpleName}: $name"

    enum class Kind {
        Class,
        Protocol,
    }

    sealed interface Origin {

        data class Kir(val kirClass: KirClass) : Origin
    }
}

fun KirClass.Kind.toOirKind(): OirClass.Kind =
    when (this) {
        KirClass.Kind.Interface -> OirClass.Kind.Protocol
        else -> OirClass.Kind.Class
    }

val OirClass.superClassType: DeclaredOirType?
    get() = superTypes.firstOrNull { it.declaration.kind == OirClass.Kind.Class }

val OirClass.superClass: OirClass?
    get() = superClassType?.declaration

fun OirClass.renderForwardDeclaration(): String =
    if (typeParameters.isEmpty()) name else "$name<${typeParameters.joinToString(", ") { it.name }}>"

val OirClass.memberFunctions: List<OirFunction>
    get() = callableDeclarations.filterIsInstance<OirFunction>()

val OirClass.constructors: List<OirConstructor>
    get() = callableDeclarations.filterIsInstance<OirConstructor>()

val OirClass.memberSimpleFunctions: List<OirSimpleFunction>
    get() = callableDeclarations.filterIsInstance<OirSimpleFunction>()

val OirClass.allFunctions: List<OirFunction>
    get() = callableDeclarationsIncludingExtensions.filterIsInstance<OirFunction>()

val OirClass.allSimpleFunctions: List<OirSimpleFunction>
    get() = callableDeclarationsIncludingExtensions.filterIsInstance<OirSimpleFunction>()

val OirClass.kirClassOrNull: KirClass?
    get() = (origin as? OirClass.Origin.Kir)?.kirClass
