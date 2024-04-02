package co.touchlab.skie.oir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.descriptor.ExtraDescriptorBuiltins
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.oir.builtin.OirBuiltins
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirExtension
import co.touchlab.skie.oir.element.OirFile
import co.touchlab.skie.oir.element.OirModule
import co.touchlab.skie.phases.oir.CreateOirTypesPhase
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.isInterface

class OirProvider(
    skieModule: KirModule,
    extraDescriptorBuiltins: ExtraDescriptorBuiltins,
    private val kirProvider: KirProvider,
    private val namer: ObjCExportNamer,
) {

    lateinit var allKotlinClasses: List<OirClass>
        private set

    lateinit var allKotlinProtocols: List<OirClass>
        private set

    lateinit var allKotlinClassesAndProtocols: List<OirClass>
        private set

    val allKotlinExtensions: List<OirExtension>
        get() = allFiles.flatMap { it.declarations }.filterIsInstance<OirExtension>()

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

    private val fileCache = mutableMapOf<Pair<OirModule, String>, OirFile>()

    private val externalClassesAndProtocolsCache = mutableMapOf<ClassDescriptor, OirClass>()

    val skieModule: OirModule.Kotlin = getModule(skieModule)

    val externalModule: OirModule.External by lazy {
        OirModule.External()
    }

    val oirBuiltins: OirBuiltins by lazy {
        OirBuiltins(this, extraDescriptorBuiltins)
    }

    fun getModule(kirModule: KirModule): OirModule.Kotlin =
        kotlinModuleCache.getOrPut(kirModule) {
            OirModule.Kotlin(kirModule.name)
        }

    fun getFile(oirModule: OirModule.Kotlin, name: String): OirFile =
        fileCache.getOrPut(oirModule to name) {
            OirFile(name, oirModule)
        }

    fun getExternalClass(descriptor: ClassDescriptor): OirClass =
        externalClassesAndProtocolsCache.getOrPut(descriptor.original) {
            val (name, kind) = if (descriptor.kind.isInterface) {
                descriptor.name.asString().removeSuffix("Protocol") to OirClass.Kind.Protocol
            } else {
                descriptor.name.asString() to OirClass.Kind.Class
            }

            val oirClass = OirClass(
                name = name,
                parent = externalModule,
                kind = kind,
                origin = OirClass.Origin.CinteropType(descriptor),
            )

            CreateOirTypesPhase.createTypeParameters(oirClass, descriptor.declaredTypeParameters, namer)

            oirClass
        }

    fun initializeKotlinClassCache() {
        allKotlinClassesAndProtocols = kirProvider.allClasses.map { it.oirClass }

        allKotlinClasses = allKotlinClassesAndProtocols.filter { it.kind == OirClass.Kind.Class }
        allKotlinProtocols = allKotlinClassesAndProtocols.filter { it.kind == OirClass.Kind.Protocol }
    }
}
