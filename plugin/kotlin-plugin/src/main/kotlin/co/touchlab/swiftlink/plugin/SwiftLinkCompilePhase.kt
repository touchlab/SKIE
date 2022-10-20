package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.resolve.DefaultTemplateVariableResolver
import co.touchlab.swiftlink.plugin.resolve.KotlinSymbolRegistry
import co.touchlab.swiftlink.plugin.resolve.KotlinSymbolResolver
import co.touchlab.swiftlink.plugin.transform.ApiNotes
import co.touchlab.swiftlink.plugin.transform.ApiTransformResolver
import co.touchlab.swiftpack.api.DefaultSkieModule
import co.touchlab.swiftpack.api.MutableSwiftTypeName
import co.touchlab.swiftpack.api.SwiftBridgedName
import co.touchlab.swiftpack.api.skieContext
import co.touchlab.swiftpack.spec.module.SwiftPackModule
import co.touchlab.swiftpack.spi.produceSwiftFile
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.TypeAliasSpec
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.konan.target.AppleConfigurables
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.platformName
import org.jetbrains.kotlin.konan.target.withOSVersion
import org.jetbrains.kotlin.library.impl.javaFile
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.io.File

internal class ApiNotesBuilder(
    private val moduleName: String,
    private val namer: ObjCExportNamer,
) {
    val typeNames = mutableMapOf<TypeTransformTarget, MutableSwiftTypeName>()

    private val mutableTypeTransforms = mutableMapOf<TypeTransformTarget, ObjcClassTransformScope>()
    val typeTransforms: Map<TypeTransformTarget, ObjcClassTransformScope> = mutableTypeTransforms

    operator fun get(descriptor: ClassDescriptor): ObjcClassTransformScope? = mutableTypeTransforms[TypeTransformTarget.Class(descriptor)]

    fun transform(descriptor: ClassDescriptor): ObjcClassTransformScope {
        val target = TypeTransformTarget.Class(descriptor)
        return mutableTypeTransforms.getOrPut(target) {
            ObjcClassTransformScope(resolveName(target))
        }
    }

    operator fun get(descriptor: PropertyDescriptor): ObjcPropertyTransformScope? = mutableTypeTransforms[descriptor.containingTarget]?.properties?.get(descriptor)

    fun transform(descriptor: PropertyDescriptor): ObjcPropertyTransformScope = typeTransform(descriptor.containingTarget).properties.getOrPut(descriptor) {
        ObjcPropertyTransformScope()
    }

    operator fun get(descriptor: FunctionDescriptor): ObjcMethodTransformScope? = mutableTypeTransforms[descriptor.containingTarget]?.methods?.get(descriptor)

    fun transform(descriptor: FunctionDescriptor): ObjcMethodTransformScope = typeTransform(descriptor.containingTarget).methods.getOrPut(descriptor) {
        ObjcMethodTransformScope()
    }

    fun resolveName(target: TypeTransformTarget): MutableSwiftTypeName = typeNames.getOrPut(target) {
        when (target) {
            is TypeTransformTarget.Class -> {
                val name = if (target.descriptor.kind == ClassKind.ENUM_ENTRY) {
                    namer.getEnumEntrySelector(target.descriptor)
                } else {
                    namer.getClassOrProtocolName(target.descriptor).swiftName
                }
                when (val parent = target.descriptor.containingDeclaration) {
                    is PackageFragmentDescriptor, is PackageViewDescriptor -> MutableSwiftTypeName(
                        originalParent = null,
                        originalSeparator = "",
                        originalSimpleName = name,
                    )
                    is ClassDescriptor -> {
                        val parentName = resolveName(TypeTransformTarget.Class(parent))
                        val parentQualifiedName = parentName.originalQualifiedName
                        val simpleNameCandidate = if (name.startsWith(parentQualifiedName)) {
                            name.drop(parentQualifiedName.length)
                        } else {
                            name
                        }
                        val (separator, simpleName) = if (simpleNameCandidate.startsWith('.')) {
                            "." to simpleNameCandidate.drop(1)
                        } else {
                            "" to simpleNameCandidate
                        }
                        MutableSwiftTypeName(
                            originalParent = parentName,
                            originalSeparator = separator,
                            originalSimpleName = simpleName,
                        )
                    }
                    else -> error("Unexpected parent type: $parent")
                }
            }
            is TypeTransformTarget.File -> {
                MutableSwiftTypeName(
                    originalParent = null,
                    originalSeparator = "",
                    originalSimpleName = namer.getFileClassName(target.file).swiftName,
                )
            }
        }

    }

    private val CallableMemberDescriptor.containingTarget: TypeTransformTarget
        get() = when (val containingDeclaration = containingDeclaration) {
            is ClassDescriptor -> TypeTransformTarget.Class(containingDeclaration)
            is PackageFragmentDescriptor -> TypeTransformTarget.File(findSourceFile())
            else -> error("Unexpected containing declaration: $containingDeclaration")
        }

    private fun typeTransform(typeTransformTarget: TypeTransformTarget): ObjcClassTransformScope {
        return mutableTypeTransforms.getOrPut(typeTransformTarget) {
            ObjcClassTransformScope(resolveName(typeTransformTarget))
        }
    }

    class ObjcClassTransformScope(
        var swiftName: MutableSwiftTypeName,
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var bridge: SwiftBridgedName? = null,
    ) {
        val newSwiftName: MutableSwiftTypeName?
            get() = swiftName.takeIf { it.isChanged }

        val properties = mutableMapOf<PropertyDescriptor, ObjcPropertyTransformScope>()
        val methods = mutableMapOf<FunctionDescriptor, ObjcMethodTransformScope>()
    }

    class ObjcPropertyTransformScope(
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var rename: String? = null,
    )

    class ObjcMethodTransformScope(
        var isRemoved: Boolean = false,
        var isHidden: Boolean = false,
        var rename: String? = null,
    )

    sealed interface TypeTransformTarget {
        data class Class(val descriptor: ClassDescriptor) : TypeTransformTarget
        data class File(val file: SourceFile): TypeTransformTarget
    }

    fun save(directory: File, enableBridging: Boolean) {
        fun typeNotes(target: TypeTransformTarget, transform: ObjcClassTransformScope): YamlBuilder = YamlBuilder().apply {
            val descriptorName = when (target) {
                is TypeTransformTarget.Class -> namer.getClassOrProtocolName(target.descriptor)
                is TypeTransformTarget.File -> namer.getFileClassName(target.file)
            }

            +"- Name: \"${descriptorName.objCName}\""

            indented {
                if (enableBridging) {
                    transform.bridge?.let { +"SwiftBridge: ${moduleName}.${it.resolve()}" }
                }
                transform.isHidden.ifTrue { +"SwiftPrivate: true" }
                transform.newSwiftName?.let { +"SwiftName: ${it.qualifiedName}" }
                transform.isRemoved.ifTrue { +"Availability: nonswift" }

                if (transform.properties.isNotEmpty()) {
                    +"Properties:"
                    transform.properties.forEach { (property, propertyTransform) ->
                        +"- Name: ${namer.getPropertyName(property)}"
                        indented {
                            +"PropertyKind: ${if (property.dispatchReceiverParameter == null) "Class" else "Instance"}"
                            propertyTransform.rename?.let { +"SwiftName: $it" }
                            propertyTransform.isHidden.ifTrue { +"SwiftPrivate: true" }
                            propertyTransform.isRemoved.ifTrue { +"Availability: nonswift" }
                        }
                    }
                }

                if (transform.methods.isNotEmpty()) {
                    +"Methods:"
                    transform.methods.forEach { (method, methodTransform) ->
                        +"- Selector: \"${namer.getSelector(method)}\""
                        indented {
                            +"MethodKind: ${if (method.dispatchReceiverParameter == null) "Class" else "Instance"}"
                            methodTransform.rename?.let { +"SwiftName: \"$it\"" }
                            methodTransform.isHidden.ifTrue { +"SwiftPrivate: true" }
                            methodTransform.isRemoved.ifTrue { +"Availability: nonswift" }
                        }
                    }
                }
            }
        }

        ensureChildClassesRenamedWhereNeeded()

        val notesByTypes = mutableTypeTransforms.mapValues { (descriptor, transform) ->
            typeNotes(descriptor, transform)
        }

        val classNotes = notesByTypes.filter { (key, _) -> key !is TypeTransformTarget.Class || !key.descriptor.kind.isInterface }
        val protocolNotes = notesByTypes.filter { (key, _) -> key is TypeTransformTarget.Class && key.descriptor.kind.isInterface }

        val builder = YamlBuilder()
        with(builder) {
            +"Name: \"$moduleName\""

            if (classNotes.isNotEmpty()) {
                +"Classes:"
            }

            classNotes.forEach { (_, notes) ->
                +notes
            }

            if (protocolNotes.isNotEmpty()) {
                +"Protocols:"
            }

            protocolNotes.forEach { (_, notes) ->
                +notes
            }
        }

        directory.resolve("${moduleName}.apinotes").writeText(builder.storage.toString())
    }

    private fun ensureChildClassesRenamedWhereNeeded() {
        fun touchNestedClassTransforms(descriptor: ClassDescriptor) {
            return descriptor.unsubstitutedMemberScope.getContributedDescriptors().filterIsInstance<ClassDescriptor>()
                .filter { it.kind == ClassKind.CLASS || it.kind == ClassKind.OBJECT }
                .forEach { childDescriptor ->
                    val target = TypeTransformTarget.Class(childDescriptor)
                    val transform = typeTransform(target)
                    assert(transform.newSwiftName != null) { "Expected to have a new name for $childDescriptor" }

                    touchNestedClassTransforms(childDescriptor)
                }
        }

        mutableTypeTransforms
            .forEach { (target, transform) ->
                if (target is TypeTransformTarget.Class && transform.newSwiftName != null) {
                    touchNestedClassTransforms(target.descriptor)
                }
            }
    }

    private class YamlBuilder(val storage: StringBuilder = StringBuilder()) {
        operator fun String.unaryPlus() {
            storage.appendLine(this)
        }

        operator fun YamlBuilder.unaryPlus() {
            this@YamlBuilder.storage.append(this.storage)
        }

        fun indented(perform: YamlBuilder.() -> Unit) {
            val builder = YamlBuilder()
            builder.perform()
            builder.storage.lines().forEach { storage.appendLine("  $it") }
        }
    }
}


class SwiftLinkCompilePhase(
    private val config: KonanConfig,
    private val context: CommonBackendContext,
    private val namer: ObjCExportNamer,
    private val swiftSourceFiles: List<File>,
    private val expandedSwiftDir: File,
) {
    private val moduleLoader = SwiftPackModuleLoader(context.configuration)

    private val SwiftPackModule.Name.sourceFilePrefix: String
        get() = when (this) {
            is SwiftPackModule.Name.Simple -> name
            is SwiftPackModule.Name.Namespaced -> "${namespace}_${name.sourceFilePrefix}"
        }

    fun process(): List<ObjectFile> {
        if (config.configuration.get(KonanConfigKeys.PRODUCE) != CompilerOutputKind.FRAMEWORK) {
            return emptyList()
        }

        val symbolRegistry = KotlinSymbolRegistry(moduleLoader.references)
        val symbolResolver = KotlinSymbolResolver(context, symbolRegistry)
        val transformResolver = ApiTransformResolver(namer, symbolResolver, moduleLoader.transforms)
        val configurables = config.platform.configurables as? AppleConfigurables ?: return emptyList()
        val swiftSourcesDir = expandedSwiftDir.also {
            it.deleteRecursively()
            it.mkdirs()
        }

        val framework = FrameworkLayout(config.outputFile)
        val apiNotesBuilder = ApiNotesBuilder(framework.moduleName, namer)
        val swiftContext = DefaultMutableSwiftContext(namer, apiNotesBuilder, framework.moduleName)
        val skieModule = context.skieContext.module as DefaultSkieModule
        skieModule.consumeConfigureBlocks().forEach {
            it(swiftContext)
        }
        val swiftFileSpecs = skieModule.produceFiles(swiftContext)

        val newFiles = swiftFileSpecs.map { fileSpec ->
            val file = swiftSourcesDir.resolve("${fileSpec.name}.swift")
            fileSpec.toString().also { file.writeText(it) }
            file
        }

        val bridgeTypeAliasesFile = createBridgeTypeAliasesFileIfNeeded(apiNotesBuilder, swiftSourcesDir)

        val sourceFiles = listOfNotNull(bridgeTypeAliasesFile) +
            produceSwiftFilesFromModules(symbolResolver, transformResolver, swiftSourcesDir) + swiftSourceFiles + newFiles

        val apiNotes = ApiNotes(framework.moduleName, transformResolver)

        val swiftObjectPaths = if (sourceFiles.isNotEmpty()) {
            // apiNotes.save(framework.headersDir, false)
            apiNotesBuilder.save(framework.headersDir, false)

            val swiftObjectsDir = config.tempFiles.create("swift-object").also { it.mkdirs() }

            compileSwift(configurables, framework, sourceFiles, swiftObjectsDir.javaFile())

            appendSwiftHeaderImportIfNeeded(framework)

            addSwiftSpecificLinkerArgs(configurables)

            swiftObjectsDir.listFilesOrEmpty.filter { it.extension == "o" }.map { it.absolutePath }
        } else {
            emptyList()
        }

        // apiNotes.save(framework.headersDir, true)
        apiNotesBuilder.save(framework.headersDir, true)

        disableWildcardExportIfNeeded(framework)

        return swiftObjectPaths
    }

    private fun createBridgeTypeAliasesFileIfNeeded(
        apiNotesBuilder: ApiNotesBuilder,
        swiftSourcesDir: File,
    ): File? {
        val bridgeTypealiases = apiNotesBuilder.typeTransforms.mapNotNull { (declaration, transform) ->
            val bridgedName = transform.bridge as? SwiftBridgedName.Relative ?: return@mapNotNull null
            TypeAliasSpec.builder(bridgedName.typealiasName, DeclaredTypeName.qualifiedTypeName(".${bridgedName.typealiasValue}"))
                .addModifiers(Modifier.PUBLIC)
                .build()
        }

        if (bridgeTypealiases.isEmpty()) {
            return null
        }

        val file = swiftSourcesDir.resolve("__ObjCBridgeTypeAliases.swift")
        FileSpec.builder("__ObjCBridgeTypeAliases")
            .apply {
                bridgeTypealiases.forEach { addType(it) }
            }
            .build()
            .toString()
            .also { file.writeText(it) }

        return file
    }

    private fun produceSwiftFilesFromModules(
        symbolResolver: KotlinSymbolResolver,
        transformResolver: ApiTransformResolver,
        swiftSourcesDir: File,
    ) = moduleLoader.modules.flatMap { module ->
        val filenamePrefix = module.name.sourceFilePrefix
        val templateVariableResolver = DefaultTemplateVariableResolver(
            config.moduleId,
            namer,
            symbolResolver,
            transformResolver,
            module.templateVariables,
        )

        module.files.map { file ->
            val finalContents = file.produceSwiftFile(templateVariableResolver)
            val targetSwiftFile = swiftSourcesDir.resolve("${filenamePrefix}_${file.name}.swift")
            targetSwiftFile.writeText(finalContents)
            targetSwiftFile
        }
    }

    private fun compileSwift(configurables: AppleConfigurables, framework: FrameworkLayout, sourceFiles: List<File>, swiftObjectsDir: File) {
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
        if (config.configuration.getBoolean(ConfigurationKeys.disableWildcardExport)) {
            framework.modulemapFile.writeText(
                framework.modulemapFile.readLines().filterNot { it.contains("export *") }.joinToString(System.lineSeparator())
            )
        }
    }
}
