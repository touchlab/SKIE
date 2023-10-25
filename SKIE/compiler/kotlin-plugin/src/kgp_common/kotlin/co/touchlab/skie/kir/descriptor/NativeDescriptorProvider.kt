@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjcExportedInterfaceReflector
import co.touchlab.skie.compilerinject.reflection.reflectors.UserVisibleIrModulesSupportReflector
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.ir.konanLibrary
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.isRecursiveInlineOrValueClassType
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.utils.ResolvedDependency

fun interface ExposedModulesProvider {

    fun exposedModules(): Set<ModuleDescriptor>
}

class NativeDescriptorProvider(
    private val exposedModulesProvider: ExposedModulesProvider,
    private val konanConfig: KonanConfig,
    private val exportedInterface: ObjcExportedInterfaceReflector,
) : DescriptorProvider {

    override val builtIns: KotlinBuiltIns by lazy {
        exposedModules.first().builtIns
    }

    override val extraDescriptorBuiltins: ExtraDescriptorBuiltins by lazy {
        ExtraDescriptorBuiltins(exposedModules)
    }

    override val exposedModules: Set<ModuleDescriptor> by lazy {
        exposedModulesProvider.exposedModules()
    }

    private val mutableExposedClasses by lazy {
        exportedInterface.generatedClasses.filterNot { it.defaultType.isRecursiveInlineOrValueClassType() }.toMutableSet()
    }

    override val exposedClasses: Set<ClassDescriptor> by ::mutableExposedClasses

    private val mutableTopLevel by lazy {
        exportedInterface.topLevel
            .mapValues { it.value.toMutableList() }
            .filter { it.value.isNotEmpty() }
            .toMutableMap()
    }

    override val exposedFiles: Set<SourceFile>
        get() = mutableTopLevel.keys.toSet()

    private val mutableExposedCategoryMembers by lazy {
        exportedInterface.categoryMembers
            .mapValues { it.value.toMutableList() }
            .filter { it.value.isNotEmpty() }
            .toMutableMap()
    }

    override val exposedCategoryMembers: Set<CallableMemberDescriptor>
        get() = mutableExposedCategoryMembers.values.flatten().toSet()

    override val exposedTopLevelMembers: Set<CallableMemberDescriptor>
        get() = mutableTopLevel.values.flatten().toSet()

    private val mapper: ObjCExportMapper by lazy {
        exportedInterface.mapper
    }

    override val externalDependencies: Set<ResolvedDependency> by lazy {
        konanConfig.userVisibleIrModulesSupport
            .reflectedBy<UserVisibleIrModulesSupportReflector>()
            .externalDependencyModules
            .toSet()
    }

    override val resolvedLibraries: List<KotlinLibrary> by lazy {
        konanConfig.resolvedLibraries.getFullList()
    }

    override val buildInLibraries: Set<KotlinLibrary> by lazy {
        resolvedLibraries.filter { it.isDefault }.toSet()
    }

    override val externalLibraries: Set<KotlinLibrary> by lazy {
        val externalLibrariesArtifacts = externalDependencies.flatMap { it.artifactPaths }.map { it.path }.toSet()

        resolvedLibraries
            .filter { it.libraryFile.absolutePath in externalLibrariesArtifacts }
            .toSet() - buildInLibraries
    }

    override val localLibraries: Set<KotlinLibrary> by lazy {
        resolvedLibraries.toSet() - buildInLibraries - externalLibraries
    }

    override fun isFromLocalModule(declarationDescriptor: DeclarationDescriptor): Boolean =
        declarationDescriptor.module.konanLibrary in localLibraries

    override fun isExposed(callableMemberDescriptor: CallableMemberDescriptor): Boolean =
        callableMemberDescriptor.isExposed

    override fun isExposable(callableMemberDescriptor: CallableMemberDescriptor): Boolean =
        callableMemberDescriptor.isExposable

    override fun isExposable(classDescriptor: ClassDescriptor): Boolean =
        classDescriptor.isExposable

    override fun isBaseMethod(functionDescriptor: FunctionDescriptor): Boolean =
        mapper.isBaseMethod(functionDescriptor)

    override fun isBaseProperty(propertyDescriptor: PropertyDescriptor): Boolean =
        mapper.isBaseProperty(propertyDescriptor)

    @get:JvmName("isExposableExtension")
    private val CallableMemberDescriptor.isExposable: Boolean
        get() = mapper.shouldBeExposed(this)

    @get:JvmName("isExposableExtension")
    private val ClassDescriptor.isExposable: Boolean
        get() = mapper.shouldBeExposed(this)

    @get:JvmName("isExposedExtension")
    private val CallableMemberDescriptor.isExposed: Boolean
        get() = this in exposedTopLevelMembers ||
            this in exposedCategoryMembers ||
            (this.containingDeclaration in exposedClasses && this.isExposable)

    fun registerExposedDescriptor(descriptor: DeclarationDescriptor) {
        when (descriptor) {
            is ClassDescriptor -> registerDescriptor(descriptor)
            is CallableMemberDescriptor -> registerDescriptor(descriptor)
            else -> error("Unsupported descriptor type: $descriptor.")
        }
    }

    private fun registerDescriptor(descriptor: ClassDescriptor) {
        if (!descriptor.isExposable) {
            return
        }

        mutableExposedClasses.add(descriptor)
    }

    private fun registerDescriptor(descriptor: CallableMemberDescriptor) {
        if (!descriptor.isExposable) {
            return
        }

        if (descriptor.containingDeclaration is ClassDescriptor) {
            return
        }

        if (mapper.isTopLevel(descriptor)) {
            val descriptors = mutableTopLevel.getOrPut(descriptor.findSourceFile()) {
                mutableListOf()
            }

            descriptors.add(descriptor)
        } else {
            val categoryClass = mapper.getClassIfCategory(descriptor) ?: error("$descriptor is neither top level nor category.")
            val descriptors = mutableExposedCategoryMembers.getOrPut(categoryClass) {
                mutableListOf()
            }

            descriptors.add(descriptor)
        }
    }

    override fun getFileModule(file: SourceFile): ModuleDescriptor =
        mutableTopLevel[file]?.firstOrNull()?.module ?: error("File $file is not known to contain exported top level declarations.")

    override fun getExposedClassMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
        classDescriptor.unsubstitutedMemberScope
            .getDescriptorsFiltered()
            .filterIsInstance<CallableMemberDescriptor>()
            .filter { it.isExposed }

    override fun getExposedCategoryMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
        mutableExposedCategoryMembers[classDescriptor] ?: emptyList()

    override fun getExposedConstructors(classDescriptor: ClassDescriptor): List<ClassConstructorDescriptor> =
        classDescriptor.constructors.filter { it.isExposed }

    override fun getExposedStaticMembers(file: SourceFile): List<CallableMemberDescriptor> =
        mutableTopLevel[file] ?: emptyList()

    override fun getReceiverClassDescriptorOrNull(descriptor: CallableMemberDescriptor): ClassDescriptor? {
        val categoryClass = mapper.getClassIfCategory(descriptor)
        val containingDeclaration = descriptor.containingDeclaration

        return when {
            categoryClass != null -> categoryClass
            descriptor is PropertyAccessorDescriptor -> {
                if (mapper.isObjCProperty(descriptor.correspondingProperty)) {
                    getReceiverClassDescriptorOrNull(descriptor.correspondingProperty)
                } else {
                    null
                }
            }
            containingDeclaration is ClassDescriptor -> containingDeclaration
            else -> null
        }
    }

    override fun getExposedCompanionObject(classDescriptor: ClassDescriptor): ClassDescriptor? =
        classDescriptor.companionObjectDescriptor?.takeIf { it.isExposable }

    override fun getExposedNestedClasses(classDescriptor: ClassDescriptor): List<ClassDescriptor> =
        classDescriptor.unsubstitutedInnerClassesScope
            .getDescriptorsFiltered(DescriptorKindFilter.CLASSIFIERS)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.CLASS || it.kind == ClassKind.OBJECT || it.kind == ClassKind.ENUM_CLASS }
            .filter { it.isExposable }

    override fun getExposedEnumEntries(classDescriptor: ClassDescriptor): List<ClassDescriptor> =
        classDescriptor.unsubstitutedInnerClassesScope
            .getDescriptorsFiltered(DescriptorKindFilter.CLASSIFIERS)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.ENUM_ENTRY }
}
