package co.touchlab.skie.oir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.element.KirModule
import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirExtension
import co.touchlab.skie.oir.element.OirFile
import co.touchlab.skie.oir.element.OirModule

class OirProvider(
    private val kirProvider: KirProvider,
) {

    val externalModule = OirModule.External()

    lateinit var kotlinClasses: List<OirClass>
        private set

    lateinit var kotlinProtocols: List<OirClass>
        private set

    lateinit var kotlinClassesAndProtocols: List<OirClass>
        private set

    val kotlinExtensions: List<OirExtension>
        get() = allFiles.flatMap { it.declarations }.filterIsInstance<OirExtension>()

    lateinit var externalClasses: List<OirClass>
        private set

    lateinit var externalProtocols: List<OirClass>
        private set

    lateinit var externalClassesAndProtocols: List<OirClass>
        private set

    val allClasses: List<OirClass>
        get() = kotlinClasses + externalClasses

    val allProtocols: List<OirClass>
        get() = kotlinProtocols + externalProtocols

    val allClassesAndProtocols: List<OirClass>
        get() = allClasses + allProtocols

    val allFiles: Collection<OirFile>
        get() = fileCache.values

    private val kotlinModuleCache = mutableMapOf<KirModule, OirModule.Kotlin>()

    private val fileCache = mutableMapOf<Pair<OirModule, String>, OirFile>()

    // TODO Remove after not needed for ApiNotes, do not use to store classes
    val skieModule: OirModule.Kotlin = OirModule.Kotlin("Skie")

    fun getKotlinModule(kirModule: KirModule): OirModule.Kotlin =
        kotlinModuleCache.getOrPut(kirModule) {
            when (kirModule.origin) {
                KirModule.Origin.Kotlin,
                KirModule.Origin.SkieRuntime,
                KirModule.Origin.SkieGenerated,
                -> {
                }
                KirModule.Origin.KnownExternal, KirModule.Origin.UnknownExternal -> error("External modules are not supported: $kirModule.")
            }

            OirModule.Kotlin(kirModule.name)
        }

    fun getFile(oirModule: OirModule.Kotlin, name: String): OirFile =
        fileCache.getOrPut(oirModule to name) {
            OirFile(name, oirModule)
        }

    fun initializeKotlinClassCache() {
        kotlinClassesAndProtocols = kirProvider.kotlinClasses.map { it.oirClass }

        kotlinClasses = kotlinClassesAndProtocols.filter { it.kind == OirClass.Kind.Class }
        kotlinProtocols = kotlinClassesAndProtocols.filter { it.kind == OirClass.Kind.Protocol }
    }

    fun initializeExternalClassCache() {
        externalClassesAndProtocols = kirProvider.allExternalClasses.map { it.oirClass }

        externalClasses = externalClassesAndProtocols.filter { it.kind == OirClass.Kind.Class }
        externalProtocols = externalClassesAndProtocols.filter { it.kind == OirClass.Kind.Protocol }
    }
}
