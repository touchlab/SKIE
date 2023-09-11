package co.touchlab.skie.plugin.api.sir

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.sir.element.SirClass
import co.touchlab.skie.plugin.api.sir.element.SirExtension
import co.touchlab.skie.plugin.api.sir.element.SirFile
import co.touchlab.skie.plugin.api.sir.element.SirTypeAlias
import co.touchlab.skie.plugin.api.util.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class SirNamespaceProvider(
    private val descriptorProvider: DescriptorProvider,
    private val namer: ObjCExportNamer,
    private val sirProvider: SirProvider,
) {

    private val classNamespaceCache = mutableMapOf<ClassDescriptor, SirClass>()

    private val fileNamespaceCache = mutableMapOf<SourceFile, SirClass>()

    private val moduleNamespaceCache = mutableMapOf<ModuleDescriptor, SirClass>()

    private val skieNamespaceBaseClass: SirClass by lazy {
        SirClass(
            simpleName = "Skie",
            parent = sirProvider.getFile(SirFile.skieNamespace, "Skie"),
            kind = SirClass.Kind.Enum,
        )
    }

    private val modulesWithShortNameCollision =
        descriptorProvider.exposedModules
            .groupBy { it.shortSkieModuleName }
            .filter { it.value.size > 1 }
            .values
            .flatten()
            .toSet()

    fun getFile(swiftModel: KotlinTypeSwiftModel): SirFile =
        sirProvider.getFile(swiftModel.skieNamespaceName, swiftModel.skieFileName)

    private fun getFile(classDescriptor: ClassDescriptor): SirFile =
        sirProvider.getFile(classDescriptor.skieNamespaceName, classDescriptor.skieFileName)

    fun getOrCreateNamespace(classDescriptor: ClassDescriptor): SirClass =
        classNamespaceCache.getOrPut(classDescriptor) {
            val parent = if (classDescriptor in descriptorProvider.exposedClasses) {
                SirExtension(
                    typeDeclaration = getNamespaceParent(classDescriptor),
                    parent = getFile(classDescriptor),
                )
            } else {
                getNamespaceParent(classDescriptor)
            }

            SirClass(
                simpleName = classDescriptor.name.identifier.toValidNamespaceIdentifier(),
                parent = parent,
                kind = SirClass.Kind.Enum,
            )
        }

    private fun getNamespaceParent(classDescriptor: ClassDescriptor): SirClass {
        val containingClass = classDescriptor.containingDeclaration as? ClassDescriptor

        return if (containingClass != null) {
            getOrCreateNamespace(containingClass)
        } else {
            getModuleNamespace(classDescriptor.module)
        }
    }

    private fun getModuleNamespace(moduleDescriptor: ModuleDescriptor): SirClass =
        moduleNamespaceCache.getOrPut(moduleDescriptor) {
            val sirClass = SirClass(
                simpleName = moduleDescriptor.skieModuleName,
                parent = skieNamespaceBaseClass,
                kind = SirClass.Kind.Enum,
            )

            if (!moduleDescriptor.shortNameCollides) {
                SirTypeAlias(
                    simpleName = moduleDescriptor.fullSkieModuleName,
                    parent = skieNamespaceBaseClass,
                ) {
                    sirClass.defaultType
                }
            }

            sirClass
        }

    fun getOrCreateNamespace(sourceFile: SourceFile): SirClass =
        fileNamespaceCache.getOrPut(sourceFile) {
            val module = descriptorProvider.getFileModule(sourceFile)

            SirClass(
                simpleName = namer.getFileClassName(sourceFile).swiftName.toValidNamespaceIdentifier(),
                parent = getModuleNamespace(module),
                kind = SirClass.Kind.Enum,
            )
        }

    private val ModuleDescriptor.skieModuleName: String
        get() {
            return if (this.shortNameCollides) this.fullSkieModuleName else this.shortSkieModuleName
        }

    private val KotlinTypeSwiftModel.skieNamespaceName: String
        get() = when (val descriptorHolder = descriptorHolder) {
            is ClassOrFileDescriptorHolder.Class -> descriptorHolder.value.skieNamespaceName
            is ClassOrFileDescriptorHolder.File -> descriptorProvider.getFileModule(descriptorHolder.value).skieModuleName
        }

    private val ClassDescriptor.skieNamespaceName: String
        get() = module.skieModuleName

    private val ModuleDescriptor.shortNameCollides: Boolean
        get() = this in modulesWithShortNameCollision

    private val KotlinTypeSwiftModel.skieFileName: String
        get() = when (val descriptorHolder = descriptorHolder) {
            is ClassOrFileDescriptorHolder.Class -> descriptorHolder.value.skieFileName
            is ClassOrFileDescriptorHolder.File -> namer.getFileClassName(descriptorHolder.value).swiftName
        }

    private fun String.toValidNamespaceIdentifier(): String {
        val defaultName = this.toValidSwiftIdentifier()

        return if (defaultName == sirProvider.sirBuiltins.Skie.module.name) defaultName + "_" else defaultName
    }

    @Suppress("RecursivePropertyAccessor")
    private val ClassDescriptor.skieFileName: String
        get() = ((this.containingDeclaration as? ClassDescriptor)?.skieFileName?.let { "$it." } ?: "") + this.name.identifier.toValidSwiftIdentifier()

    private val ModuleDescriptor.shortSkieModuleName: String
        get() = (this.stableName ?: this.name).asStringStripSpecialMarkers().substringAfter(":")
            .changeNamingConventionToPascalCase()
            .toValidNamespaceIdentifier()

    private val ModuleDescriptor.fullSkieModuleName: String
        get() = (this.stableName ?: this.name).asStringStripSpecialMarkers()
            .replace(":", "__")
            .toValidNamespaceIdentifier()
}

private fun String.changeNamingConventionToPascalCase(): String =
    splitToSequence("_", "-")
        .map { it.replaceFirstChar(Char::uppercase) }
        .joinToString("")
