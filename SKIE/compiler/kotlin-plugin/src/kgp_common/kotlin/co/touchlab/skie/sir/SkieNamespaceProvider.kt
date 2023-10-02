package co.touchlab.skie.sir

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.kir.modulesWithExposedDeclarations
import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.sir.element.SirExtension
import co.touchlab.skie.sir.element.SirFile
import co.touchlab.skie.sir.element.SirTypeAlias
import co.touchlab.skie.swiftmodel.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.util.swift.toValidSwiftIdentifier
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.descriptorUtil.module

class SkieNamespaceProvider(
    private val descriptorProvider: DescriptorProvider,
    private val namer: ObjCExportNamer,
    private val sirProvider: SirProvider,
) {

    private val classNamespaceCache = mutableMapOf<ClassDescriptor, SirClass>()

    private val fileNamespaceCache = mutableMapOf<SourceFile, SirClass>()

    private val moduleNamespaceCache = mutableMapOf<ModuleDescriptor, SirClass>()

    private val skieNamespaceFile = sirProvider.getFile(SirFile.skieNamespace, "Skie")

    private val skieNamespaceBaseClass: SirClass = SirClass(
        simpleName = "Skie",
        parent = skieNamespaceFile,
        kind = SirClass.Kind.Enum,
    )

    init {
        // Ensures at least one file imports Foundation
        skieNamespaceFile.imports.add("Foundation")
    }

    private val modulesWithShortNameCollision =
        descriptorProvider.modulesWithExposedDeclarations
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
                    classDeclaration = getNamespaceParent(classDescriptor),
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
        get() = ((this.containingDeclaration as? ClassDescriptor)?.skieFileName?.let { "$it." }
            ?: "") + this.name.identifier.toValidSwiftIdentifier()

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
