package co.touchlab.skie.kir

import co.touchlab.skie.configuration.GlobalConfiguration
import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.kir.element.KirOverridableDeclaration
import co.touchlab.skie.kir.element.KirProject
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirProperty

class KirProvider(delegate: Lazy<KirProviderDelegate>, globalConfiguration: GlobalConfiguration) {

    private lateinit var fqNameCache: Map<String, KirClass>

    private lateinit var sirToCallableDeclarationsCache: Map<SirCallableDeclaration, KirCallableDeclaration<*>>

    private lateinit var sirToEnumEntryCache: Map<SirProperty, KirEnumEntry>

    private val delegate by delegate

    val kotlinModules: Collection<KirModule>
        get() = delegate.kotlinModules

    val allExternalClasses: Collection<KirClass>
        get() = delegate.allExternalClassesAndProtocols

    val allPlatformClasses: Collection<KirClass>
        get() = delegate.allExternalClassesAndProtocols.filter { it.origin == KirClass.Origin.PlatformType }

    val allClasses: Set<KirClass>
        get() = kotlinClasses + allExternalClasses

    lateinit var kotlinClasses: Set<KirClass>
        private set

    lateinit var kotlinEnums: Set<KirClass>
        private set

    lateinit var kotlinCallableDeclarations: List<KirCallableDeclaration<*>>
        private set

    lateinit var kotlinFunctions: List<KirFunction<*>>
        private set

    lateinit var kotlinSimpleFunctions: List<KirSimpleFunction>
        private set

    lateinit var kotlinConstructors: List<KirConstructor>
        private set

    lateinit var kotlinOverridableDeclaration: List<KirOverridableDeclaration<*, *>>
        private set

    val project = KirProject(globalConfiguration)

    val kirBuiltins: KirBuiltins
        get() = delegate.kirBuiltins

    val stdlibModule: KirModule
        get() = delegate.stdlibModule

    fun initializeKotlinClassCache() {
        val visitedClasses = mutableSetOf<KirClass>()

        kotlinModules.flatMap { it.classes }.forEach {
            cacheClassesRecursively(it, visitedClasses)
        }

        kotlinClasses = visitedClasses.toSet()
        kotlinEnums = kotlinClasses.filter { it.kind == KirClass.Kind.Enum }.toSet()

        fqNameCache = kotlinClasses.associateBy { it.kotlinFqName }
    }

    private fun cacheClassesRecursively(kirClass: KirClass, visitedClasses: MutableSet<KirClass>) {
        visitedClasses.add(kirClass)

        kirClass.classes.forEach { cacheClassesRecursively(it, visitedClasses) }
    }

    fun initializeCallableDeclarationsCache() {
        kotlinCallableDeclarations = kotlinClasses.flatMap { it.callableDeclarations }

        kotlinFunctions = kotlinCallableDeclarations.filterIsInstance<KirFunction<*>>()
        kotlinSimpleFunctions = kotlinFunctions.filterIsInstance<KirSimpleFunction>()
        kotlinConstructors = kotlinFunctions.filterIsInstance<KirConstructor>()
        kotlinOverridableDeclaration = kotlinCallableDeclarations.filterIsInstance<KirOverridableDeclaration<*, *>>()
    }

    fun initializeSirCallableDeclarationsCache() {
        sirToCallableDeclarationsCache = kotlinCallableDeclarations.associateBy { it.originalSirDeclaration } +
            kotlinCallableDeclarations.filter { it.bridgedSirDeclaration != null }.associateBy { it.bridgedSirDeclaration!! }

        sirToEnumEntryCache = kotlinEnums.flatMap { it.enumEntries }.associateBy { it.sirEnumEntry }
    }

    fun getClassByFqName(fqName: String): KirClass = findClassByFqName(fqName)
        ?: error("Class not found: $fqName. This error usually means that the class is not exposed to Objective-C.")

    fun findClassByFqName(fqName: String): KirClass? = fqNameCache[fqName]

    // TODO Refactor to Origin
    @Suppress("UNCHECKED_CAST")
    fun <S : SirCallableDeclaration> findCallableDeclaration(callableDeclaration: SirCallableDeclaration): KirCallableDeclaration<S>? =
        sirToCallableDeclarationsCache[callableDeclaration] as KirCallableDeclaration<S>?

    fun findEnumEntry(property: SirProperty): KirEnumEntry? = sirToEnumEntryCache[property]
}
