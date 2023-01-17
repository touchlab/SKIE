@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.plugin.getAllExportedModuleDescriptors
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.getBaseMethods
import org.jetbrains.kotlin.backend.konan.objcexport.getBaseProperties
import org.jetbrains.kotlin.backend.konan.objcexport.getClassIfCategory
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class NativeDescriptorProvider(private val context: CommonBackendContext) : DescriptorProvider {

    private val mutableClassDescriptors by lazy {
        exportedInterface.generatedClasses.toMutableSet()
    }

    override val classDescriptors: Set<ClassDescriptor> by ::mutableClassDescriptors

    private val mutableExportedClassDescriptors by lazy {
        classDescriptors.filter { it.isExported }.toMutableSet()
    }

    override val exportedClassDescriptors: Set<ClassDescriptor> by ::mutableExportedClassDescriptors

    private val mutableCategoryMembersDescriptors by lazy {
        exportedInterface.categoryMembers
            .mapValues { it.value.filter { descriptor -> descriptor.isExported }.toMutableSet() }
            .filter { it.value.isNotEmpty() }
            .toMutableMap()
    }

    override val exportedFiles: Set<SourceFile>
        get() = mutableTopLevel.keys

    private val mutableTopLevel by lazy {
        exportedInterface.topLevel
            .mapValues { it.value.filter { descriptor -> descriptor.isExported }.toMutableSet() }
            .filter { it.value.isNotEmpty() }
            .toMutableMap()
    }

    override val exportedCategoryMembersCallableDescriptors: Set<CallableMemberDescriptor>
        get() = mutableCategoryMembersDescriptors.values.flatten().toSet()

    override val exportedTopLevelCallableDescriptors: Set<CallableMemberDescriptor>
        get() = mutableTopLevel.values.flatten().toSet()

    private val exportedModules: Set<ModuleDescriptor> by lazy {
        context.getAllExportedModuleDescriptors().toSet()
    }

    val mapper: ObjCExportMapper by lazy {
        exportedInterface.mapper
    }

    private val DeclarationDescriptor.isExported: Boolean
        get() = this.module in exportedModules

    private val exportedInterface by lazy {
        val objCExport = ObjCExportReflector.new(context)

        objCExport.reflectedExportedInterface
    }

    override fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean =
        mapper.shouldBeExposed(descriptor)

    override fun shouldBeExposed(descriptor: ClassDescriptor): Boolean =
        mapper.shouldBeExposed(descriptor)

    override fun registerDescriptor(descriptor: DeclarationDescriptor) {
        when (descriptor) {
            is ClassDescriptor -> registerDescriptor(descriptor)
            is CallableMemberDescriptor -> registerDescriptor(descriptor)
            else -> error("Unsupported descriptor type: $descriptor.")
        }
    }

    private fun registerDescriptor(descriptor: ClassDescriptor) {
        if (!shouldBeExposed(descriptor) || !descriptor.isExported) {
            return
        }

        mutableClassDescriptors.add(descriptor)
        mutableExportedClassDescriptors.add(descriptor)
    }

    private fun registerDescriptor(descriptor: CallableMemberDescriptor) {
        if (!shouldBeExposed(descriptor) || !descriptor.isExported) {
            return
        }

        if (descriptor.containingDeclaration is ClassDescriptor) {
            return
        }

        if (mapper.isTopLevel(descriptor)) {
            val descriptors = mutableTopLevel.getOrPut(descriptor.findSourceFile()) {
                mutableSetOf()
            }

            descriptors.add(descriptor)
        } else {
            val categoryClass = mapper.getClassIfCategory(descriptor) ?: error("$descriptor is neither top level nor category.")
            val descriptors = mutableCategoryMembersDescriptors.getOrPut(categoryClass) {
                mutableSetOf()
            }

            descriptors.add(descriptor)
        }
    }

    override fun getModuleForFile(file: SourceFile): ModuleDescriptor =
        mutableTopLevel[file]?.firstOrNull()?.module ?: error("File $file is not known to contain exported top level declarations.")

    override fun getExposedBaseMethods(classDescriptor: ClassDescriptor): List<FunctionDescriptor> =
        classDescriptor.unsubstitutedMemberScope
            .getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { shouldBeExposed(it) }
            .filter { mapper.isBaseMethod(it) }

    override fun getFirstBaseMethodForAllExposedMethods(classDescriptor: ClassDescriptor): List<FunctionDescriptor> =
        classDescriptor.unsubstitutedMemberScope
            .getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<SimpleFunctionDescriptor>()
            .filter { shouldBeExposed(it) }
            .map { mapper.getBaseMethods(it).first() }

    override fun getExposedConstructors(classDescriptor: ClassDescriptor): List<ConstructorDescriptor> =
        classDescriptor.constructors
            .filter { shouldBeExposed(it) }

    override fun getExposedBaseProperties(classDescriptor: ClassDescriptor): List<PropertyDescriptor> =
        classDescriptor.unsubstitutedMemberScope
            .getDescriptorsFiltered()
            .filterIsInstance<PropertyDescriptor>()
            .filter { shouldBeExposed(it) }
            .filter { mapper.isBaseProperty(it) }

    override fun getFirstBasePropertyForAllExposedProperties(classDescriptor: ClassDescriptor): List<PropertyDescriptor> =
        classDescriptor.unsubstitutedMemberScope
            .getDescriptorsFiltered()
            .filterIsInstance<PropertyDescriptor>()
            .filter { shouldBeExposed(it) }
            .map { mapper.getBaseProperties(it).first() }

    override fun getExposedFileContent(file: SourceFile): Set<CallableMemberDescriptor> =
        mutableTopLevel[file] ?: emptySet()

    override fun getExposedCategoryMembers(classDescriptor: ClassDescriptor): Set<CallableMemberDescriptor> =
        mutableCategoryMembersDescriptors[classDescriptor] ?: emptySet()
}
