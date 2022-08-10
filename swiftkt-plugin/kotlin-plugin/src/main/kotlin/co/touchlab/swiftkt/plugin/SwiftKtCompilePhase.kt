package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spec.KobjcTransforms
import co.touchlab.swiftpack.spec.KotlinFileReference
import co.touchlab.swiftpack.spec.KotlinFunctionReference
import co.touchlab.swiftpack.spec.KotlinPropertyReference
import co.touchlab.swiftpack.spec.KotlinTypeReference
import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
import co.touchlab.swiftpack.spi.SwiftNameProvider
import co.touchlab.swiftpack.spi.produceSwiftFile
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.platformName
import org.jetbrains.kotlin.konan.target.withOSVersion
import org.jetbrains.kotlin.library.impl.javaFile
import java.io.File

fun List<KobjcTransforms>.merge(): KobjcTransforms {
    val types = this.flatMap { it.types.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val files = this.flatMap { it.files.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val properties = this.flatMap { it.properties.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val functions = this.flatMap { it.functions.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }

    return KobjcTransforms(types, files, properties, functions)
}

fun List<KobjcTransforms.TypeTransform>.merge(reference: KotlinTypeReference): KobjcTransforms.TypeTransform {
    val properties = this.flatMap { it.properties.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }
    val functions = this.flatMap { it.methods.entries }.groupBy({ it.key }, { it.value }).mapValues { it.value.merge(it.key) }

    return KobjcTransforms.TypeTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
        bridge = mapNotNull { it.bridge }.singleOrNull(),
        properties = properties,
        methods = functions,
    )
}

fun List<KobjcTransforms.FileTransform>.merge(reference: KotlinFileReference): KobjcTransforms.FileTransform {
    return KobjcTransforms.FileTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
        bridge = mapNotNull { it.bridge }.singleOrNull(),
    )
}

fun List<KobjcTransforms.PropertyTransform>.merge(reference: KotlinPropertyReference): KobjcTransforms.PropertyTransform {
    return KobjcTransforms.PropertyTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
    )
}

fun List<KobjcTransforms.FunctionTransform>.merge(reference: KotlinFunctionReference): KobjcTransforms.FunctionTransform {
    return KobjcTransforms.FunctionTransform(
        reference = reference,
        hide = any { it.hide },
        remove = any { it.remove },
        rename = mapNotNull { it.rename }.singleOrNull(),
    )
}

class SwiftKtCompilePhase(
    val swiftPackModuleReferences: List<NamespacedSwiftPackModule.Reference>,
    val swiftSourceFiles: List<File>,
    val expandedSwiftDir: File,
) {
    fun process(config: KonanConfig, context: CommonBackendContext, namer: ObjCExportNamer): List<ObjectFile> {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return emptyList()
        }
        val swiftPackModules = swiftPackModuleReferences.flatMap { (namespace, moduleFile) ->
            if (moduleFile.isDirectory) {
                moduleFile.listFiles()?.map {
                    NamespacedSwiftPackModule(namespace, SwiftPackModule.read(it))
                } ?: emptyList()
            } else {
                listOf(NamespacedSwiftPackModule(namespace, SwiftPackModule.read(moduleFile)))
            }
        }
        val kotlinNameResolver = KotlinNameResolver(context)
        val transformResolver = TransformResolver(
            namer,
            kotlinNameResolver,
            swiftPackModules,
        )
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()
        val swiftSourcesDir = expandedSwiftDir.also {
            it.deleteRecursively()
            it.mkdirs()
        }

        val sourceFiles = swiftPackModules.flatMap { (namespace, module) ->
            val referenceResolver = SwiftReferenceResolver(module.references)
            val swiftNameProvider = ObjCExportNamerSwiftNameProvider(namer, kotlinNameResolver, transformResolver, referenceResolver)
            module.files.map { file ->
                val finalContents = file.produceSwiftFile(swiftNameProvider)
                val targetSwiftFile = swiftSourcesDir.resolve("${namespace}_${module.name}_${file.name}.swift")
                targetSwiftFile.writeText(finalContents)
                targetSwiftFile
            }
        } + swiftSourceFiles

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
        val headersDir = framework.resolve("Headers")
        val swiftModule = framework.resolve("Modules").resolve("$moduleName.swiftmodule").also { it.mkdirs() }
        val modulemapFile = framework.resolve("Modules/module.modulemap")
        val apiNotes = ApiNotes(moduleName, transformResolver)
        apiNotes.save(headersDir)

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
            +headersDir.resolve("$moduleName-Swift.h").absolutePath
            if (swiftcBitcodeArg != null) {
                +swiftcBitcodeArg
            }
            +swiftcBuildTypeArgs
            +"-emit-object"
            +"-enable-library-evolution"
            +"-parse-as-library"
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

        // TODO: Generate .swiftmodule and .swiftinterface for other architectures of the platform to fix missing Xcode code completion

        val swiftLibSearchPaths = listOf(
            File(configurables.absoluteTargetToolchain, "usr/lib/swift/${configurables.platformName().lowercase()}"),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }
        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift"
        )

        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)

        return swiftObjectsDir.listFiles.map { it.absolutePath }
    }
}
