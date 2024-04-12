package co.touchlab.skie.kir.element

import co.touchlab.skie.configuration.ClassConfiguration
import co.touchlab.skie.kir.type.DeclarationBackedKirType
import co.touchlab.skie.kir.type.DeclaredKirType
import co.touchlab.skie.kir.type.KirType
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.sir.element.SirClass

class KirClass(
    val kotlinFqName: String,
    val objCName: String,
    val swiftName: String,
    val parent: KirClassParent,
    val kind: Kind,
    val origin: Origin,
    superTypes: List<DeclarationBackedKirType>,
    val isSealed: Boolean,
    val hasUnexposedSealedSubclasses: Boolean,
    val configuration: ClassConfiguration,
) : KirClassParent, KirBridgeableDeclaration<SirClass> {

    lateinit var oirClass: OirClass

    override val classes: MutableList<KirClass> = mutableListOf()

    val kotlinIdentifier: String by lazy {
        kotlinFqName.substringAfterLast('.')
    }

    var companionObject: KirClass? = null

    val callableDeclarations: MutableList<KirCallableDeclaration<*>> = mutableListOf()

    val enumEntries: MutableList<KirEnumEntry> = mutableListOf()

    val sealedSubclasses: MutableList<KirClass> = mutableListOf()

    val superTypes: MutableList<DeclarationBackedKirType> = superTypes.toMutableList()

    val typeParameters: MutableList<KirTypeParameter> = mutableListOf()

    override val module: KirModule
        get() = parent.module

    override val originalSirDeclaration: SirClass
        get() = oirClass.originalSirClass

    // Should not be directly accessed before all bridging configuration is done. See @MustBeExecutedAfterBridgingConfiguration.
    override val primarySirDeclaration: SirClass
        get() = oirClass.primarySirClass

    // Should not be directly accessed before all bridging configuration is done. See @MustBeExecutedAfterBridgingConfiguration.
    override var bridgedSirDeclaration: SirClass?
        get() = oirClass.bridgedSirClass
        set(value) {
            oirClass.bridgedSirClass = value
        }

    val originalSirClass: SirClass by ::originalSirDeclaration

    val primarySirClass: SirClass by ::primarySirDeclaration

    var bridgedSirClass: SirClass? by ::bridgedSirDeclaration

    init {
        parent.classes.add(this)
    }

    val defaultType: DeclaredKirType by lazy {
        toType(emptyList())
    }

    fun toType(typeArguments: List<KirType>): DeclaredKirType =
        DeclaredKirType(this, typeArguments = typeArguments)

    fun toType(vararg typeArguments: KirType): DeclaredKirType =
        toType(typeArguments.toList())

    override fun toString(): String = "${this::class.simpleName}: $kotlinFqName"

    enum class Kind {
        Class, Interface, File, Enum, Object, CompanionObject
    }

    sealed interface Origin {

        object Kotlin : Origin

        object PlatformType : Origin

        object ExternalCinteropType : Origin
    }
}

val KirClass.superClassType: DeclaredKirType?
    get() = superTypes.firstOrNull {
        val declaredType = it as? DeclaredKirType ?: return@firstOrNull false

        declaredType.declaration.kind != KirClass.Kind.Interface
    } as DeclaredKirType?

val KirClass.superClass: KirClass?
    get() = superClassType?.declaration
