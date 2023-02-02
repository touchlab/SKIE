@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.plugin.getAllExportedModuleDescriptors
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.isRecursiveInlineOrValueClassType
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class NativeDescriptorProvider(private val context: CommonBackendContext) : DescriptorProvider {

    private val mutableTransitivelyExposedClasses by lazy {
        exportedInterface.generatedClasses.filterNot { it.defaultType.isRecursiveInlineOrValueClassType() }.toMutableList()
    }

    override val transitivelyExposedClasses: List<ClassDescriptor> by ::mutableTransitivelyExposedClasses

    private val mutableExposedClasses by lazy {
        transitivelyExposedClasses.filter { it.isExported }.toMutableList()
    }

    override val exposedClasses: List<ClassDescriptor> by ::mutableExposedClasses

    private val mutableTopLevel by lazy {
        exportedInterface.topLevel
            .mapValues { it.value.filter { descriptor -> descriptor.isExported }.toMutableList() }
            .filter { it.value.isNotEmpty() }
            .toMutableMap()
    }

    override val exposedFiles: List<SourceFile>
        get() = mutableTopLevel.keys.toList()

    private val mutableExposedCategoryMembers by lazy {
        exportedInterface.categoryMembers
            .mapValues { it.value.filter { descriptor -> descriptor.isExported }.toMutableList() }
            .filter { it.value.isNotEmpty() }
            .toMutableMap()
    }

    override val exposedCategoryMembers: List<CallableMemberDescriptor>
        get() = mutableExposedCategoryMembers.values.flatten()

    override val exposedTopLevelMembers: List<CallableMemberDescriptor>
        get() = mutableTopLevel.values.flatten()

    val mapper: ObjCExportMapper by lazy {
        exportedInterface.mapper
    }

    private val exportedModules: List<ModuleDescriptor> by lazy {
        context.getAllExportedModuleDescriptors()
    }

    private val DeclarationDescriptor.isExported: Boolean
        get() = this.module in exportedModules

    private val exportedInterface by lazy {
        val objCExport = ObjCExportReflector.new(context)

        objCExport.reflectedExportedInterface
    }

    @get:JvmName("isExposedExtension")
    private val CallableMemberDescriptor.isExposed: Boolean
        get() = mapper.shouldBeExposed(this) && this.isExported

    @get:JvmName("isExposedExtension")
    private val ClassDescriptor.isExposed: Boolean
        get() = mapper.shouldBeExposed(this) && this.isExported

    override fun isExposed(descriptor: CallableMemberDescriptor): Boolean =
        descriptor.isExposed

    override fun isExposed(descriptor: ClassDescriptor): Boolean =
        descriptor.isExposed

    override fun isTransitivelyExposed(descriptor: ClassDescriptor): Boolean =
        mapper.shouldBeExposed(descriptor)

    override fun registerExposedDescriptor(descriptor: DeclarationDescriptor) {
        when (descriptor) {
            is ClassDescriptor -> registerDescriptor(descriptor)
            is CallableMemberDescriptor -> registerDescriptor(descriptor)
            else -> error("Unsupported descriptor type: $descriptor.")
        }
    }

    private fun registerDescriptor(descriptor: ClassDescriptor) {
        if (!isExposed(descriptor)) {
            return
        }

        mutableTransitivelyExposedClasses.add(descriptor)
        mutableExposedClasses.add(descriptor)
    }

    private fun registerDescriptor(descriptor: CallableMemberDescriptor) {
        if (!descriptor.isExposed) {
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
        classDescriptor.companionObjectDescriptor?.takeIf { it.isExposed }

    override fun getExposedNestedClasses(classDescriptor: ClassDescriptor): List<ClassDescriptor> =
        classDescriptor.unsubstitutedInnerClassesScope
            .getDescriptorsFiltered(DescriptorKindFilter.CLASSIFIERS)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.CLASS || it.kind == ClassKind.OBJECT }
            .filter { it.isExposed }

    override fun getExposedEnumEntries(classDescriptor: ClassDescriptor): List<ClassDescriptor> =
        classDescriptor.unsubstitutedInnerClassesScope
            .getDescriptorsFiltered(DescriptorKindFilter.CLASSIFIERS)
            .filterIsInstance<ClassDescriptor>()
            .filter { it.kind == ClassKind.ENUM_ENTRY }
}
