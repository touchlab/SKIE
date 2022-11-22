@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.plugin.getAllExportedModuleDescriptors
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.resolve.descriptorUtil.module

internal class DescriptorProvider(private val context: CommonBackendContext) {

    val classDescriptors: Set<ClassDescriptor> by lazy {
        exportedInterface.generatedClasses
    }

    val exportedClassDescriptors: Set<ClassDescriptor> by lazy {
        classDescriptors.filter { it.isExported }.toSet()
    }

    val exportedCategoryMembersCallableDescriptors: Set<CallableMemberDescriptor> by lazy {
        exportedInterface.categoryMembers.values.flatten().toSet().filter { it.isExported }.toSet()
    }

    val exportedTopLevelCallableDescriptors: Set<CallableMemberDescriptor> by lazy {
        exportedInterface.topLevel.values.flatten().toSet().filter { it.isExported }.toSet()
    }

    private val exportedModules: Set<ModuleDescriptor> by lazy {
        context.getAllExportedModuleDescriptors().toSet()
    }

    val mapper: ObjCExportMapper by lazy {
        exportedInterface.mapper
    }

    private val DeclarationDescriptor.isExported: Boolean
        get() = this.module in exportedModules

    fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean =
        mapper.shouldBeExposed(descriptor)

    private val exportedInterface by lazy {
        val objCExport = ObjCExportReflector.new(context)

        objCExport.reflectedExportedInterface
    }
}
