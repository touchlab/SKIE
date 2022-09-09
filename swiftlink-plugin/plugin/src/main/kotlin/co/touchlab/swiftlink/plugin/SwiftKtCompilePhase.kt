package co.touchlab.swiftlink.plugin

import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
import co.touchlab.swiftpack.spi.produceSwiftFile
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.isSimulator
import org.jetbrains.kotlin.konan.target.platformName
import org.jetbrains.kotlin.konan.target.withOSVersion
import org.jetbrains.kotlin.library.impl.javaFile
import java.io.File

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
        val kotlinHeader = headersDir.resolve("$moduleName.h")
        val swiftHeader = headersDir.resolve("$moduleName-Swift.h")
        val swiftModule = framework.resolve("Modules").resolve("$moduleName.swiftmodule").also { it.mkdirs() }
        val modulemapFile = framework.resolve("Modules/module.modulemap")
        val apiNotes = ApiNotes(moduleName, transformResolver)
        apiNotes.save(headersDir, sourceFiles.isEmpty())

        // We want to make sure we generate .apinotes file even if there are no Swift source files.
        if (sourceFiles.isEmpty()) {
            return emptyList()
        }

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
            +swiftHeader.absolutePath
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
        // TODO: This line seems to fix the "warning: umbrella header for module 'ExampleKit' does not include header 'ExampleKit-Swift.h'",
        // TODO: but not in all invocations, which is why it is commented out and to be investigated.
        // kotlinHeader.appendText("\n#import \"${swiftHeader.name}\"\n")

        if (targetTriple.isSimulator && configurables.target.architecture == Architecture.ARM64 && setOf(Family.IOS, Family.TVOS, Family.WATCHOS).contains(configurables.target.family)) {
            val otherTriple = targetTriple.copy(architecture = "x86_64")
            Command("${configurables.absoluteTargetToolchain}/usr/bin/swiftc").apply {
                +"-v"
                +listOf("-module-name", moduleName)
                +"-import-underlying-module"
                +listOf("-Xcc", "-fmodule-map-file=$modulemapFile")
                +"-emit-module-interface-path"
                +swiftModule.resolve("$otherTriple.swiftinterface").absolutePath
                +"-emit-module-path"
                +swiftModule.resolve("$otherTriple.swiftmodule").absolutePath
                if (swiftcBitcodeArg != null) {
                    +swiftcBitcodeArg
                }
                +swiftcBuildTypeArgs
                +"-enable-library-evolution"
                +"-parse-as-library"
                +"-g"
                +"-sdk"
                +configurables.absoluteTargetSysRoot
                +"-target"
                +otherTriple.withOSVersion(configurables.osVersionMin).toString()
                +sourceFiles.map { it.absolutePath }

                workingDirectory = swiftObjectsDir.javaFile()

                logWith {
                    println(it())
                }
                execute()
            }
        }

        apiNotes.save(headersDir, true)

        modulemapFile.writeText(
            modulemapFile.readLines().filterNot { it.contains("export *") }.joinToString(System.lineSeparator())
        )

        val swiftLibSearchPaths = listOf(
            File(configurables.absoluteTargetToolchain, "usr/lib/swift/${configurables.platformName().lowercase()}"),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }
        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift"
        )

        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)

        return swiftObjectsDir.listFilesOrEmpty.filter { it.extension == "o" }.map { it.absolutePath }
    }
}
