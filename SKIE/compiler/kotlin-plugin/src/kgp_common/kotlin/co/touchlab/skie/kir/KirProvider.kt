package co.touchlab.skie.kir

import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.kir.element.KirCallableDeclaration
import co.touchlab.skie.kir.element.KirClass
import co.touchlab.skie.kir.element.KirConstructor
import co.touchlab.skie.kir.element.KirEnumEntry
import co.touchlab.skie.kir.element.KirFunction
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.kir.element.KirOverridableDeclaration
import co.touchlab.skie.kir.element.KirProject
import co.touchlab.skie.kir.element.KirSimpleFunction
import co.touchlab.skie.kir.element.classDescriptorOrNull
import co.touchlab.skie.kir.element.sourceFileOrNull
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.runtime.isSkieKotlinRuntime
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirProperty
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class KirProvider(
    kotlinBuiltIns: KotlinBuiltIns,
    extraDescriptorBuiltins: ExtraDescriptorBuiltins,
    namer: ObjCExportNamer,
) {

    private val modulesMap = mutableMapOf<String, KirModule>()

    private lateinit var classDescriptorCache: Map<ClassDescriptor, KirClass>
    private lateinit var fileCache: Map<SourceFile, KirClass>

    private lateinit var fqNameCache: Map<String, KirClass>

    private lateinit var descriptorsToCallableDeclarationsCache: Map<CallableMemberDescriptor, KirCallableDeclaration<*>>

    private lateinit var sirToCallableDeclarationsCache: Map<SirCallableDeclaration, KirCallableDeclaration<*>>

    private lateinit var sirToEnumEntryCache: Map<SirProperty, KirEnumEntry>

    val allModules: Collection<KirModule>
        get() = modulesMap.values

    lateinit var allClasses: Set<KirClass>
        private set

    lateinit var allEnums: Set<KirClass>
        private set

    lateinit var allCallableDeclarations: List<KirCallableDeclaration<*>>
        private set

    lateinit var allFunctions: List<KirFunction<*>>
        private set

    lateinit var allSimpleFunctions: List<KirSimpleFunction>
        private set

    lateinit var allConstructors: List<KirConstructor>
        private set

    lateinit var allOverridableDeclaration: List<KirOverridableDeclaration<*, *>>
        private set

    val project = KirProject()

    val skieModule = getModule("Skie")

    private val stdlibModule = getModule(kotlinBuiltIns.string.module)

    val kirBuiltins: KirBuiltins = KirBuiltins(
        stdlibModule = stdlibModule,
        kotlinBuiltIns = kotlinBuiltIns,
        extraDescriptorBuiltins = extraDescriptorBuiltins,
        namer = namer,
    )

    private fun getModule(name: String, descriptor: ModuleDescriptor? = null): KirModule =
        modulesMap.getOrPut(name) {
            KirModule(
                name = name,
                project = project,
                descriptor = descriptor,
                isSkieKotlinRuntime = descriptor?.isSkieKotlinRuntime ?: false,
            )
        }

    fun getModule(moduleDescriptor: ModuleDescriptor): KirModule =
        getModule((moduleDescriptor.stableName ?: moduleDescriptor.name).asStringStripSpecialMarkers(), moduleDescriptor)

    fun initializeClassCache() {
        val visitedClasses = mutableSetOf<KirClass>()

        allModules.flatMap { it.classes }.forEach {
            cacheClassesRecursively(it, visitedClasses)
        }

        allClasses = visitedClasses.toSet()
        allEnums = allClasses.filter { it.kind == KirClass.Kind.Enum }.toSet()

        classDescriptorCache = allClasses.mapNotNull { kirClass -> kirClass.classDescriptorOrNull?.let { it to kirClass } }.toMap()
        fileCache = allClasses.mapNotNull { kirClass -> kirClass.sourceFileOrNull?.let { it to kirClass } }.toMap()

        fqNameCache = classDescriptorCache.mapKeys { it.key.fqNameSafe.asString() }
    }

    private fun cacheClassesRecursively(kirClass: KirClass, visitedClasses: MutableSet<KirClass>) {
        visitedClasses.add(kirClass)

        kirClass.classes.forEach { cacheClassesRecursively(it, visitedClasses) }
    }

    fun initializeCallableDeclarationsCache() {
        descriptorsToCallableDeclarationsCache = allClasses.flatMap { it.callableDeclarations }.associateBy { it.descriptor }

        allCallableDeclarations = descriptorsToCallableDeclarationsCache.values.toList()

        allFunctions = allCallableDeclarations.filterIsInstance<KirFunction<*>>()
        allSimpleFunctions = allFunctions.filterIsInstance<KirSimpleFunction>()
        allConstructors = allFunctions.filterIsInstance<KirConstructor>()
        allOverridableDeclaration = allCallableDeclarations.filterIsInstance<KirOverridableDeclaration<*, *>>()
    }

    fun initializeSirCallableDeclarationsCache() {
        sirToCallableDeclarationsCache = allCallableDeclarations.associateBy { it.originalSirDeclaration } +
            allCallableDeclarations.filter { it.bridgedSirDeclaration != null }.associateBy { it.bridgedSirDeclaration!! }

        sirToEnumEntryCache = allEnums.flatMap { it.enumEntries }.associateBy { it.sirEnumEntry }
    }

    fun getClass(classDescriptor: ClassDescriptor): KirClass =
        findClass(classDescriptor)
            ?: error("Class not found: $classDescriptor. This error usually means that the class is not exposed to Objective-C.")

    fun getClass(sourceFile: SourceFile): KirClass =
        findClass(sourceFile)
            ?: error("Class not found: $sourceFile. This error usually means that the class is not exposed to Objective-C.")

    fun getClassByFqName(fqName: String): KirClass =
        findClassByFqName(fqName)
            ?: error("Class not found: $fqName. This error usually means that the class is not exposed to Objective-C.")

    fun getFunction(functionDescriptor: FunctionDescriptor): KirSimpleFunction =
        findFunction(functionDescriptor)
            ?: error("Function not found: $functionDescriptor. This error usually means that the function is not exposed to Objective-C.")

    fun getConstructor(constructorDescriptor: ClassConstructorDescriptor): KirConstructor =
        findConstructor(constructorDescriptor)
            ?: error("Constructor not found: $constructorDescriptor. This error usually means that the constructor is not exposed to Objective-C.")

    fun findClass(classDescriptor: ClassDescriptor): KirClass? =
        classDescriptorCache[classDescriptor.original]

    fun findClass(sourceFile: SourceFile): KirClass? =
        fileCache[sourceFile]

    fun findClassByFqName(fqName: String): KirClass? =
        fqNameCache[fqName]

    @Suppress("UNCHECKED_CAST")
    fun <S : SirCallableDeclaration> findCallableDeclaration(callableDeclaration: SirCallableDeclaration): KirCallableDeclaration<S>? =
        sirToCallableDeclarationsCache[callableDeclaration] as KirCallableDeclaration<S>?

    fun findFunction(functionDescriptor: FunctionDescriptor): KirSimpleFunction? =
        descriptorsToCallableDeclarationsCache[functionDescriptor] as? KirSimpleFunction

    fun findConstructor(constructorDescriptor: ClassConstructorDescriptor): KirConstructor? =
        descriptorsToCallableDeclarationsCache[constructorDescriptor] as? KirConstructor

    fun findEnumEntry(property: SirProperty): KirEnumEntry? =
        sirToEnumEntryCache[property]
}
