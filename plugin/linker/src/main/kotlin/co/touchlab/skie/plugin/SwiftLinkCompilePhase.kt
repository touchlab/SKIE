package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.apinotes.builder.ApiNotes
import co.touchlab.skie.api.apinotes.builder.ApiNotesFactory
import co.touchlab.skie.api.apinotes.fixes.memberconflicts.CallableMembersConflictsApiNotesFix
import co.touchlab.skie.api.apinotes.fixes.ClassInsideNonExportedClassApiNotesFix
import co.touchlab.skie.api.apinotes.fixes.ClassesConflictsApiNotesFix
import co.touchlab.skie.api.apinotes.fixes.HeaderFilePropertyOrderingFix
import co.touchlab.skie.api.apinotes.fixes.memberconflicts.KonanManglingApiNotesFix
import co.touchlab.skie.api.apinotes.fixes.NestedBridgedTypesApiNotesFix
import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.api.model.DescriptorBridgeProvider
import co.touchlab.skie.api.model.type.translation.SwiftTranslationProblemCollector
import co.touchlab.skie.api.model.type.translation.SwiftTypeTranslator
import co.touchlab.skie.plugin.api.descriptorProvider
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.api.util.FrameworkLayout
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

class SwiftLinkCompilePhase(
    private val config: KonanConfig,
    private val context: CommonBackendContext,
    private val namer: ObjCExportNamer,
) {

    private val skieContext = context.skieContext

    fun process(): List<ObjectFile> {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return emptyList()
        }
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()
        val swiftSourcesDir = skieContext.expandedSwiftDir.also {
            it.deleteRecursively()
            it.mkdirs()
        }

        val framework = FrameworkLayout(config.outputFile).also { it.cleanSkie() }
        val bridgeProvider = DescriptorBridgeProvider(namer)
        val translator = SwiftTypeTranslator(
            descriptorProvider = context.descriptorProvider,
            namer = namer,
            problemCollector = SwiftTranslationProblemCollector.Default(context),
        )
        val swiftModelScope = DefaultSwiftModelScope(
            namer = namer,
            descriptorProvider = context.descriptorProvider,
            bridgeProvider = bridgeProvider,
            translator = translator,
        )
        val skieModule = skieContext.module as DefaultSkieModule

        KonanManglingApiNotesFix(skieModule, context.descriptorProvider).resetNames()
        CallableMembersConflictsApiNotesFix(skieModule, context.descriptorProvider).fixNames()
        ClassesConflictsApiNotesFix(skieModule, context.descriptorProvider).fixNames()
        NestedBridgedTypesApiNotesFix(skieModule, context.descriptorProvider).createTypeAliasesForBridgingFile()
        ClassInsideNonExportedClassApiNotesFix(skieModule, context.descriptorProvider).renameProblematicClasses()
        HeaderFilePropertyOrderingFix().reorderHeaderFile(framework.kotlinHeader)

        skieModule.consumeConfigureBlocks(swiftModelScope)
        val swiftFileSpecs = skieModule.produceSwiftPoetFiles(swiftModelScope)
        val swiftTextFiles = skieModule.produceTextFiles()

        val newFiles = swiftFileSpecs.map { fileSpec ->
            val file = swiftSourcesDir.resolve("${fileSpec.name}.swift")
            fileSpec.toString().also { file.writeText(it) }
            file
        } + swiftTextFiles.map { textFile ->
            val file = swiftSourcesDir.resolve("${textFile.name}.swift")
            file.writeText(textFile.content)
            file
        }

        val sourceFiles = skieContext.swiftSourceFiles + newFiles

        val apiNotes = ApiNotesFactory(framework.moduleName, context.descriptorProvider, swiftModelScope).create()

        apiNotes.createApiNotesFile(framework)

        val swiftObjectPaths = if (sourceFiles.isNotEmpty()) {
            val swiftObjectsDir = config.tempFiles.create("swift-object").also { it.mkdirs() }

            compileSwift(configurables, framework, sourceFiles, swiftObjectsDir.javaFile())

            appendSwiftHeaderImportIfNeeded(framework)

            addSwiftSpecificLinkerArgs(configurables)

            swiftObjectsDir.listFilesOrEmpty.filter { it.extension == "o" }.map { it.absolutePath }
        } else {
            emptyList()
        }

        disableWildcardExportIfNeeded(framework)

        return swiftObjectPaths
    }

    private fun ApiNotes.createApiNotesFile(framework: FrameworkLayout) {
        val content = this.createApiNotesFileContent()

        val apiNotesFile = framework.headersDir.resolve("${framework.moduleName}.apinotes")

        apiNotesFile.writeText(content)
    }

    private fun compileSwift(
        configurables: AppleConfigurables,
        framework: FrameworkLayout,
        sourceFiles: List<File>,
        swiftObjectsDir: File,
    ) {
        val targetTriple = configurables.targetTriple

        Command("${configurables.absoluteTargetToolchain}/usr/bin/swiftc").apply {
            +listOf("-module-name", framework.moduleName)
            +"-import-underlying-module"
            +listOf("-Xcc", "-fmodule-map-file=${framework.modulemapFile}")
            +"-emit-module-interface-path"
            +framework.swiftModule.resolve("$targetTriple.swiftinterface").absolutePath
            +"-emit-module-path"
            +framework.swiftModule.resolve("$targetTriple.swiftmodule").absolutePath
            +"-emit-objc-header-path"
            +framework.swiftHeader.absolutePath
            getSwiftcBitcodeArg()?.let { +it }
            +getSwiftcBuildTypeArgs()
            +"-emit-object"
            +"-enable-library-evolution"
            +"-parse-as-library"
            +"-g"
            +"-sdk"
            +configurables.absoluteTargetSysRoot
            +"-target"
            +targetTriple.withOSVersion(configurables.osVersionMin).toString()
            +sourceFiles.map { it.absolutePath }

            workingDirectory = swiftObjectsDir

            execute()
        }
    }

    private fun getSwiftcBitcodeArg() = when (config.configuration.get(KonanConfigKeys.BITCODE_EMBEDDING_MODE)) {
        BitcodeEmbedding.Mode.NONE, null -> null
        BitcodeEmbedding.Mode.FULL -> "-embed-bitcode"
        BitcodeEmbedding.Mode.MARKER -> "-embed-bitcode-marker"
    }

    private fun getSwiftcBuildTypeArgs() = if (config.debug) {
        emptyList()
    } else {
        listOf("-O", "-whole-module-optimization")
    }

    private fun appendSwiftHeaderImportIfNeeded(framework: FrameworkLayout) {
        if (framework.swiftHeader.exists()) {
            // TODO: This line seems to fix the "warning: umbrella header for module 'ExampleKit' does not include header 'ExampleKit-Swift.h'",
            // TODO: but not in all invocations, which is why it needs to be investigated (for example running link for both dynamic and static).
            framework.kotlinHeader.appendText("\n#import \"${framework.swiftHeader.name}\"\n")
        }
    }

    private fun addSwiftSpecificLinkerArgs(configurables: AppleConfigurables) {
        val swiftLibSearchPaths = listOf(
            File(configurables.absoluteTargetToolchain, "usr/lib/swift/${configurables.platformName().lowercase()}"),
            File(configurables.absoluteTargetSysRoot, "usr/lib/swift"),
        ).flatMap { listOf("-L", it.absolutePath) }
        val otherLinkerFlags = listOf(
            "-rpath", "/usr/lib/swift"
        )

        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, swiftLibSearchPaths)
        config.configuration.addAll(KonanConfigKeys.LINKER_ARGS, otherLinkerFlags)
    }

    private fun disableWildcardExportIfNeeded(framework: FrameworkLayout) {
        if (skieContext.disableWildcardExport) {
            framework.modulemapFile.writeText(
                framework.modulemapFile.readLines().filterNot { it.contains("export *") }.joinToString(System.lineSeparator())
            )
        }
    }
}
