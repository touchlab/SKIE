package co.touchlab.skie.sir

import co.touchlab.skie.swiftmodel.type.translation.BuiltinSwiftBridgeableProvider
import co.touchlab.skie.swiftmodel.type.translation.SwiftTypeTranslator
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.swiftmodel.SwiftModelScope
import co.touchlab.skie.swiftmodel.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirDeclaration
import co.touchlab.skie.sir.element.SirDeclarationParent
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirTypeDeclaration
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import java.nio.file.Path

class SirProvider(
    private val namer: ObjCExportNamer,
    framework: FrameworkLayout,
    private val descriptorProvider: DescriptorProvider,
    sdkPath: String,
    reporter: Reporter,
) {

    val translator: SwiftTypeTranslator by lazy {
        SwiftTypeTranslator(
            descriptorProvider = descriptorProvider,
            namer = namer,
            reporter = reporter,
            builtinSwiftBridgeableProvider = BuiltinSwiftBridgeableProvider(
                sdkPath = sdkPath,
                sirProvider = this,
            ),
            sirProvider = this,
        )
    }

    private val kotlinBuiltinsModule: SirModule.KotlinBuiltins = SirModule.KotlinBuiltins("stdlib")

    private val kotlinModule: SirModule.Kotlin = SirModule.Kotlin(framework.moduleName)

    private val skieModule: SirModule.Skie = SirModule.Skie(framework.moduleName)

    val sirBuiltins by lazy {
        SirBuiltins(kotlinBuiltinsModule, kotlinModule, skieModule, this, namer)
    }

    private val skieNamespaceProvider by lazy {
        SkieNamespaceProvider(descriptorProvider, namer, this)
    }

    private val kotlinSirClassFactory by lazy {
        KotlinSirClassFactory(
            sirProvider = this,
            translator = translator,
            namespaceProvider = skieNamespaceProvider,
            namer = namer,
            descriptorProvider = descriptorProvider,
        )
    }

    private val externalModuleCache = mutableMapOf<String, SirModule.External>()

    private val externalClassesByDescriptorCache = mutableMapOf<DeclarationDescriptor, SirClass>()

    private val externalClassesByNameCache = mutableMapOf<SirFqName, SirClass>()

    private val fileByPathCache = mutableMapOf<Path, SirFile>()

    val files: Collection<SirFile>
        get() = skieModule.files

    private val allLocalDeclarations: List<SirDeclaration>
        get() = (skieModule.files + skieModule + kotlinModule + kotlinBuiltinsModule).getChildDeclarationsRecursively()

    val allLocalTypes: List<SirTypeDeclaration>
        get() = allLocalDeclarations.filterIsInstance<SirTypeDeclaration>()

    val allLocalPublicTypes: List<SirTypeDeclaration>
        get() = allLocalTypes.filter { it.visibility == SirVisibility.Public }

    val allExternalTypes: List<SirTypeDeclaration>
        get() = externalModuleCache.values.getChildDeclarationsRecursively().filterIsInstance<SirTypeDeclaration>()

    val allExternalTypesFromNonBuiltinModules: List<SirTypeDeclaration>
        get() = (externalModuleCache.values - sirBuiltins.Foundation.module - sirBuiltins.Swift.module)
            .getChildDeclarationsRecursively().filterIsInstance<SirTypeDeclaration>()

    context(SwiftModelScope)
    fun finishInitialization() {
        kotlinSirClassFactory.finishInitialization()
    }

    fun getFile(namespace: String, name: String): SirFile =
        fileByPathCache.getOrPut(SirFile.relativePath(namespace, name)) {
            SirFile(namespace, name, skieModule)
        }

    fun getFile(swiftModel: KotlinTypeSwiftModel): SirFile =
        skieNamespaceProvider.getFile(swiftModel)

    fun getSkieNamespace(swiftModel: KotlinTypeSwiftModel): SirClass =
        when (val descriptorHolder = swiftModel.descriptorHolder) {
            is ClassOrFileDescriptorHolder.Class -> skieNamespaceProvider.getOrCreateNamespace(descriptorHolder.value)
            is ClassOrFileDescriptorHolder.File -> skieNamespaceProvider.getOrCreateNamespace(descriptorHolder.value)
        }

    fun getExternalModule(moduleName: String): SirModule.External =
        externalModuleCache.getOrPut(moduleName) {
            SirModule.External(moduleName)
        }

    fun getExternalTypeDeclaration(descriptor: ClassDescriptor): SirClass =
        externalClassesByDescriptorCache.getOrPut(descriptor) {
            val moduleName = descriptor.fqNameSafe.pathSegments()[1].asString()

            val (simpleName, kind) = if (descriptor.kind.isInterface) {
                descriptor.name.asString().removeSuffix("Protocol") to SirClass.Kind.Protocol
            } else {
                descriptor.name.asString() to SirClass.Kind.Class
            }

            val module = getExternalModule(moduleName)
            val fqName = SirFqName(module, simpleName)

            getExternalTypeDeclaration(fqName, kind)
        }

    fun getExternalTypeDeclaration(fqName: SirFqName, kind: SirClass.Kind): SirClass =
        externalClassesByNameCache.getOrPut(fqName) {
            val parent = fqName.parent?.let {
                // Protocols can't have nested types and Kotlin can only reference Obj-C classes, so it cannot be Struct or Enum.
                getExternalTypeDeclaration(it, SirClass.Kind.Class)
            } ?: fqName.module

            val existingClass = parent.declarations.filterIsInstance<SirClass>().firstOrNull { it.fqName == fqName }
            if (existingClass != null) {
                return@getOrPut existingClass
            }

            return@getOrPut SirClass(
                simpleName = fqName.simpleName,
                parent = parent,
                kind = kind,
                superTypes = if (kind == SirClass.Kind.Class) listOf(sirBuiltins.Foundation.NSObject.defaultType) else emptyList(),
            )
        }

    fun getKotlinSirClass(classDescriptor: ClassDescriptor): SirClass =
        kotlinSirClassFactory.getKotlinSirClass(classDescriptor)

    fun createKotlinSirClass(sourceFile: SourceFile): SirClass =
        kotlinSirClassFactory.createKotlinSirClass(sourceFile)
}

private fun SirDeclarationParent.getChildDeclarationsRecursively(): List<SirDeclaration> =
    declarations.flatMap { it.getAllDeclarationsRecursively() }

private fun SirDeclaration.getAllDeclarationsRecursively(): List<SirDeclaration> =
    listOf(this) + if (this is SirDeclarationParent) getChildDeclarationsRecursively() else emptyList()

private fun Collection<SirDeclarationParent>.getChildDeclarationsRecursively(): List<SirDeclaration> =
    flatMap { it.getChildDeclarationsRecursively() }
