package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
import co.touchlab.swiftpack.spi.SwiftNameProvider
import co.touchlab.swiftpack.spi.produceSwiftFile
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.Checker
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.NamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import java.io.File
import org.jetbrains.kotlin.library.impl.javaFile
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.withOSVersion
import org.jetbrains.kotlin.konan.target.platformName
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.resolveClassByFqName
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.FqName

class SwiftKtObjectFilesPhase(
    private val originalPhase: CompilerPhase<CommonBackendContext, Unit, Unit>,
    private val swiftKtCompilePhase: SwiftKtCompilePhase,
    private val onInvokeCompleted: () -> Unit
): SameTypeCompilerPhase<CommonBackendContext, Unit> {
    override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext, input: Unit) {
        originalPhase.invoke(phaseConfig, phaserState, context, input)
        val config = context.javaClass.getMethod("getConfig").invoke(context) as KonanConfig
        val objCExport = context.javaClass.getMethod("getObjCExport").invoke(context)
        val namer = objCExport.javaClass.getField("namer").get(objCExport) as ObjCExportNamer?
        swiftKtCompilePhase.process(config, context, namer ?: error("namer is null"))
        onInvokeCompleted()
    }

    override val stickyPostconditions: Set<Checker<Unit>>
        get() = originalPhase.stickyPostconditions

    override fun getNamedSubphases(startDepth: Int): List<Pair<Int, NamedCompilerPhase<CommonBackendContext, *>>> {
        return originalPhase.getNamedSubphases(startDepth)
    }
}

class ObjCExportNamerSwiftNameProvider(
    private val namer: ObjCExportNamer,
    private val context: CommonBackendContext,
): SwiftNameProvider {
    override fun getSwiftName(kotlinClassName: String): String {
        val descriptor = checkNotNull(context.ir.irModule.descriptor.resolveClassByFqName(FqName(kotlinClassName), NoLookupLocation.FROM_BACKEND)) {
            "Couldn't resolve class descriptor for $kotlinClassName"
        }

        val swiftName = namer.getClassOrProtocolName(descriptor)

        return swiftName.swiftName
    }
}

class SwiftKtCompilePhase(
    val swiftPackModules: List<NamespacedSwiftPackModule>,
    val expandedSwiftDir: File,
) {
    fun process(config: KonanConfig, context: CommonBackendContext, namer: ObjCExportNamer) {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return
        }
        val configurables = config.platform.configurables as? AppleConfigurables ?: return

        val swiftNameProvider = ObjCExportNamerSwiftNameProvider(namer, context)
        val swiftSourcesDir = expandedSwiftDir.also {
            it.deleteRecursively()
            it.mkdirs()
        }
        val sourceFiles = swiftPackModules.flatMap { (namespace, module) ->
            module.files.map { file ->
                val finalContents = file.produceSwiftFile(swiftNameProvider)
                val targetSwiftFile = swiftSourcesDir.resolve("${namespace}_${module.name}_${file.name}.swift")
                targetSwiftFile.writeText(finalContents)
                targetSwiftFile
            }
        }

        val (swiftcBitcodeArg, _) = when (config.configuration.get(KonanConfigKeys.BITCODE_EMBEDDING_MODE)) {
            BitcodeEmbedding.Mode.NONE, null -> null to emptyList()
            BitcodeEmbedding.Mode.FULL -> "-embed-bitcode" to listOf("-bitcode_bundle")
            BitcodeEmbedding.Mode.MARKER -> "-embed-bitcode-marker" to listOf("-bitcode_bundle", "-bitcode_process_mode", "marker")
        }
        val swiftcBuildTypeArgs = if (config.debug) {
            emptyList()
        } else {
            listOf("-O", "-whole-module-optimization")
        }

        val framework = File(config.outputFile)
        val moduleName = framework.name.removeSuffix(".framework")
        val swiftModule = framework.resolve("Modules").resolve("$moduleName.swiftmodule").also { it.mkdirs() }
        val modulemapFile = framework.resolve("Modules/module.modulemap")

        val swiftObjectsDir = config.tempFiles.create("swift-object").also { it.mkdirs() }

        val targetTriple = configurables.targetTriple

        Command("${configurables.absoluteTargetToolchain}/usr/bin/swiftc").apply {
            +"-v"
            +listOf("-module-name", moduleName)
            +"-import-underlying-module"
            +listOf("-Xcc", "-fmodule-map-file=$modulemapFile")
            +"-emit-module-interface-path"
            +swiftModule.resolve("$targetTriple.swiftinterface").absolutePath
            +"-emit-module-path"
            +swiftModule.resolve("$targetTriple.swiftmodule").absolutePath
            +"-emit-objc-header-path"
            +framework.resolve("Headers").resolve("$moduleName-Swift.h").absolutePath
            if (swiftcBitcodeArg != null) {
                +swiftcBitcodeArg
            }
            +swiftcBuildTypeArgs
            +"-emit-object"
            +"-enable-library-evolution"
            +"-g"
            +"-sdk"
            +configurables.absoluteTargetSysRoot
            +"-target"
            +targetTriple.withOSVersion(configurables.osVersionMin).toString()
            +sourceFiles.map { it.absolutePath }

            workingDirectory = swiftObjectsDir.javaFile()

            logWith {
                println(it())
            }
            execute()
        }

        val swiftLibSearchPaths = listOf(
            File(configurables.absoluteTargetToolchain, "usr/lib/swift/${configurables.platformName().lowercase()}"),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }

        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftObjectsDir.listFiles.map { it.absolutePath })

        // return PreLinkResult(
        //     additionalObjectFiles = swiftObjectsDir.listFiles.map { it.absolutePath },
        //     additionalLinkerFlags = swiftLibSearchPaths,
        // )
    }
}
