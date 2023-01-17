package co.touchlab.skie.plugin.api.kotlin

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface DescriptorProvider {

    val classDescriptors: Set<ClassDescriptor>

    val exportedClassDescriptors: Set<ClassDescriptor>

    val exportedFiles: Set<SourceFile>

    val exportedCategoryMembersCallableDescriptors: Set<CallableMemberDescriptor>

    val exportedTopLevelCallableDescriptors: Set<CallableMemberDescriptor>

    fun shouldBeExposed(descriptor: CallableMemberDescriptor): Boolean

    fun shouldBeExposed(descriptor: ClassDescriptor): Boolean

    fun registerDescriptor(descriptor: DeclarationDescriptor)

    fun getModuleForFile(file: SourceFile): ModuleDescriptor

    fun getExposedBaseMethods(classDescriptor: ClassDescriptor): List<FunctionDescriptor>

    fun getFirstBaseMethodForAllExposedMethods(classDescriptor: ClassDescriptor): List<FunctionDescriptor>

    fun getExposedConstructors(classDescriptor: ClassDescriptor): List<ConstructorDescriptor>

    fun getExposedBaseProperties(classDescriptor: ClassDescriptor): List<PropertyDescriptor>

    fun getFirstBasePropertyForAllExposedProperties(classDescriptor: ClassDescriptor): List<PropertyDescriptor>

    fun getExposedFileContent(file: SourceFile): Set<CallableMemberDescriptor>

    fun getExposedCategoryMembers(classDescriptor: ClassDescriptor): Set<CallableMemberDescriptor>
}
