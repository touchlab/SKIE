@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.apinotes.builder

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.backend.konan.objcexport.getBaseMethods
import org.jetbrains.kotlin.backend.konan.objcexport.getBaseProperties
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

// TODO Merge into DescriptorProvider and refactor usages
internal class HierarchicalDescriptorProvider(
    private val descriptorProvider: DescriptorProvider,
    private val mapper: ObjCExportMapper,
) {

    val classes: Set<ClassDescriptor>
        get() = descriptorProvider.classDescriptors.filterNot { it.kind.isInterface }.toSet()

    val interfaces: Set<ClassDescriptor>
        get() = descriptorProvider.classDescriptors.filter { it.kind.isInterface }.toSet()

    val exportedFiles: Set<SourceFile>
        get() = descriptorProvider.exportedFiles

    fun exportedBaseFunctions(containingDescriptorHolder: ClassOrFileDescriptorHolder): Set<FunctionDescriptor> =
        exportedMembers(containingDescriptorHolder)
            .filterIsInstance<FunctionDescriptor>()
            .map { mapper.getBaseMethods(it).first() }
            .toSet()

    fun exportedBaseProperties(containingDescriptorHolder: ClassOrFileDescriptorHolder): Set<PropertyDescriptor> =
        exportedMembers(containingDescriptorHolder)
            .filterIsInstance<PropertyDescriptor>()
            .map { mapper.getBaseProperties(it).first() }
            .toSet()

    private fun exportedMembers(containingDescriptorHolder: ClassOrFileDescriptorHolder): List<CallableMemberDescriptor> =
        when (containingDescriptorHolder) {
            is ClassOrFileDescriptorHolder.Class -> exportedMembers(containingDescriptorHolder.value)
            is ClassOrFileDescriptorHolder.File -> exportedMembers(containingDescriptorHolder.value)
        }

    private fun exportedMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> {
        val methods = classDescriptor.unsubstitutedMemberScope
            .getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            .filterIsInstance<CallableMemberDescriptor>()

        val constructors = classDescriptor.constructors

        val extensions = descriptorProvider.getExportedCategoryMembers(classDescriptor)

        val allMembers = methods + constructors + extensions

        return allMembers.filter { descriptorProvider.shouldBeExposed(it) }
    }

    private fun exportedMembers(file: SourceFile): List<CallableMemberDescriptor> =
        descriptorProvider.getExportedFileContent(file)
            .filter { descriptorProvider.shouldBeExposed(it) }
}
