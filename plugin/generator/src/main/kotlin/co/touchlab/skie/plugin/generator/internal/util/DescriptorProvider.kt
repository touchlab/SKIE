@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.plugin.getAllExportedModuleDescriptors
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.serialization.findSourceFile
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.backend.konan.objcexport.isTopLevel
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.resolve.descriptorUtil.module

internal class DescriptorProvider(private val context: CommonBackendContext) {

    private val mutableClassDescriptors by lazy {
        exportedInterface.generatedClasses.toMutableSet()
    }

    val classDescriptors: Set<ClassDescriptor> by ::mutableClassDescriptors

    private val mutableExportedClassDescriptors by lazy {
        classDescriptors.filter { it.isExported }.toMutableSet()
    }

    val exportedClassDescriptors: Set<ClassDescriptor> by ::mutableExportedClassDescriptors

    private val mutableExportedCategoryMembersCallableDescriptors by lazy {
        exportedInterface.categoryMembers.values.flatten().toSet().filter { it.isExported }.toMutableSet()
    }

    val exportedCategoryMembersCallableDescriptors: Set<CallableMemberDescriptor> by ::mutableExportedCategoryMembersCallableDescriptors

    private val mutableTopLevelFiles by lazy {
        exportedInterface.topLevel.keys.toMutableSet()
    }

    val topLevelFiles: Set<SourceFile> by ::mutableTopLevelFiles

    private val mutableExportedTopLevelCallableDescriptors by lazy {
        exportedInterface.topLevel.values.flatten().toSet().filter { it.isExported }.toMutableSet()
    }

    val exportedTopLevelCallableDescriptors: Set<CallableMemberDescriptor> by ::mutableExportedTopLevelCallableDescriptors

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

    fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean =
        mapper.shouldBeExposed(descriptor)

    fun registerDescriptor(descriptor: DeclarationDescriptor) {
        when (descriptor) {
            is ClassDescriptor -> registerDescriptor(descriptor)
            is CallableMemberDescriptor -> registerDescriptor(descriptor)
            else -> error("Unsupported descriptor type: $descriptor.")
        }
    }

    private fun registerDescriptor(descriptor: ClassDescriptor) {
        if (!mapper.shouldBeExposed(descriptor) || !descriptor.isExported) {
            return
        }

        mutableClassDescriptors.add(descriptor)
        mutableExportedClassDescriptors.add(descriptor)
    }

    private fun registerDescriptor(descriptor: CallableMemberDescriptor) {
        if (!mapper.shouldBeExposed(descriptor) || !descriptor.isExported) {
            return
        }

        if (descriptor.containingDeclaration is ClassDescriptor) {
            return
        }

        if (mapper.isTopLevel(descriptor)) {
            mutableExportedTopLevelCallableDescriptors.add(descriptor)
            mutableTopLevelFiles.add(descriptor.findSourceFile())
        } else {
            mutableExportedCategoryMembersCallableDescriptors.add(descriptor)
        }
    }
}
