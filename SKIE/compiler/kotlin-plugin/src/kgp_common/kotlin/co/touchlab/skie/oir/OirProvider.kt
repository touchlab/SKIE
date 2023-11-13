package co.touchlab.skie.oir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.oir.builtin.OirBuiltins
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirFile
import co.touchlab.skie.oir.element.OirModule
import co.touchlab.skie.phases.oir.CreateOirTypesPhase
import co.touchlab.skie.sir.element.SirClass
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class OirProvider(
    skieModule: KirModule,
    extraDescriptorBuiltins: ExtraDescriptorBuiltins,
    private val kirProvider: KirProvider,
    private val namer: ObjCExportNamer,
) {

    val allExternalModules: Collection<OirModule.External>
        get() = externalModuleCache.values

    lateinit var allKotlinClasses: List<OirClass>
        private set

    lateinit var allKotlinProtocols: List<OirClass>
        private set

    lateinit var allKotlinClassesAndProtocols: List<OirClass>
        private set

    private lateinit var sirClassCache: Map<SirClass, OirClass>

    val allExternalClasses: List<OirClass>
        get() = allExternalClassesAndProtocols.filter { it.kind == OirClass.Kind.Class }

    val allExternalProtocols: List<OirClass>
        get() = allExternalClassesAndProtocols.filter { it.kind == OirClass.Kind.Protocol }

    val allExternalClassesAndProtocols: Collection<OirClass>
        get() = externalClassesAndProtocolsCache.values

    val allClasses: List<OirClass>
        get() = allKotlinClasses + allExternalClasses

    val allProtocols: List<OirClass>
        get() = allKotlinProtocols + allExternalProtocols

    val allClassesAndProtocols: List<OirClass>
        get() = allClasses + allProtocols

    val allFiles: Collection<OirFile>
        get() = fileCache.values

    private val kotlinModuleCache = mutableMapOf<KirModule, OirModule.Kotlin>()

    private val externalModuleCache = mutableMapOf<String, OirModule.External>()

    private val fileCache = mutableMapOf<Pair<OirModule, String>, OirFile>()

    private val externalClassesAndProtocolsCache = mutableMapOf<ClassDescriptor, OirClass>()

    private val externalClassesAndProtocolsFqNameCache = mutableMapOf<Pair<String, String>, OirClass>()

    val skieModule: OirModule.Kotlin = getModule(skieModule)

    // Must be last stored property - cannot be initialized lazily but requires OirProvider to be fully initialized
    val oirBuiltins: OirBuiltins = OirBuiltins(this, extraDescriptorBuiltins)

    fun getModule(kirModule: KirModule): OirModule.Kotlin =
        kotlinModuleCache.getOrPut(kirModule) {
            OirModule.Kotlin(kirModule.name)
        }

    fun getExternalModule(moduleName: String): OirModule.External =
        externalModuleCache.getOrPut(moduleName) {
            OirModule.External(moduleName)
        }

    fun getFile(oirModule: OirModule.Kotlin, name: String): OirFile =
        fileCache.getOrPut(oirModule to name) {
            OirFile(name, oirModule)
        }

    fun getExternalClass(descriptor: ClassDescriptor, module: OirModule.External? = null): OirClass =
        externalClassesAndProtocolsCache.getOrPut(descriptor.original) {
            val externalModule = module ?: run {
                val moduleName = descriptor.fqNameSafe.pathSegments()[1].asString()

                getExternalModule(moduleName)
            }

            val (name, kind) = if (descriptor.kind.isInterface) {
                descriptor.name.asString().removeSuffix("Protocol") to OirClass.Kind.Protocol
            } else {
                descriptor.name.asString() to OirClass.Kind.Class
            }

            val oirClass = OirClass(name, externalModule, kind)

            CreateOirTypesPhase.createTypeParameters(oirClass, descriptor.declaredTypeParameters, namer)

            externalClassesAndProtocolsFqNameCache[externalModule.name to name] = oirClass

            oirClass
        }

    fun initializeKotlinClassCache() {
        allKotlinClassesAndProtocols = kirProvider.allClasses.map { it.oirClass }

        allKotlinClasses = allKotlinClassesAndProtocols.filter { it.kind == OirClass.Kind.Class }
        allKotlinProtocols = allKotlinClassesAndProtocols.filter { it.kind == OirClass.Kind.Protocol }
    }

    fun initializeSirClassCache() {
        sirClassCache = allClassesAndProtocols.associateBy { it.originalSirClass }
    }

    fun findExistingExternalOirClass(moduleName: String, name: String): OirClass? =
        externalClassesAndProtocolsFqNameCache[moduleName to name]

    fun findClass(sirClass: SirClass): OirClass? =
        sirClassCache[sirClass]
}
