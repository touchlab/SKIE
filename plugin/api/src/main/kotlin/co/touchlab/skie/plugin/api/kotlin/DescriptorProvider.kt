package co.touchlab.skie.plugin.api.kotlin

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface DescriptorProvider {

    val classDescriptors: Set<ClassDescriptor>

    val exportedClassDescriptors: Set<ClassDescriptor>

    val exportedFiles: Set<SourceFile>

    val exportedCategoryMembersCallableDescriptors: Set<CallableMemberDescriptor>

    val exportedTopLevelCallableDescriptors: Set<CallableMemberDescriptor>

    fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean

    fun registerDescriptor(descriptor: DeclarationDescriptor)

    fun getModuleForFile(file: SourceFile): ModuleDescriptor

    fun getExportedFileContent(file: SourceFile): Set<CallableMemberDescriptor>

    fun getExportedCategoryMembers(classDescriptor: ClassDescriptor): Set<CallableMemberDescriptor>
}
