package co.touchlab.skie.plugin.api.kotlin

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface DescriptorProvider {

    val exposedModules: Set<ModuleDescriptor>

    val exposedClasses: Set<ClassDescriptor>

    val exposedFiles: Set<SourceFile>

    val exposedCategoryMembers: Set<CallableMemberDescriptor>

    val exposedTopLevelMembers: Set<CallableMemberDescriptor>

    fun isExposed(callableMemberDescriptor: CallableMemberDescriptor): Boolean

    fun isExposable(callableMemberDescriptor: CallableMemberDescriptor): Boolean

    fun isExposable(classDescriptor: ClassDescriptor): Boolean

    fun getFileModule(file: SourceFile): ModuleDescriptor

    /**
     * Functions/properties without extensions and constructors (including overridden ones)
     */
    fun getExposedClassMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor>

    /**
     * Functions/properties extensions for classes (not interfaces or generics)
     */
    fun getExposedCategoryMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor>

    fun getExposedConstructors(classDescriptor: ClassDescriptor): List<ClassConstructorDescriptor>

    fun getExposedStaticMembers(file: SourceFile): List<CallableMemberDescriptor>

    fun getReceiverClassDescriptorOrNull(descriptor: CallableMemberDescriptor): ClassDescriptor?

    fun getExposedCompanionObject(classDescriptor: ClassDescriptor): ClassDescriptor?

    fun getExposedNestedClasses(classDescriptor: ClassDescriptor): List<ClassDescriptor>

    fun getExposedEnumEntries(classDescriptor: ClassDescriptor): List<ClassDescriptor>
}

fun DescriptorProvider.getAllExposedMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
    this.getExposedClassMembers(classDescriptor) +
        this.getExposedCategoryMembers(classDescriptor) +
        this.getExposedConstructors(classDescriptor)

val DescriptorProvider.allExposedMembers: List<CallableMemberDescriptor>
    get() = (this.exposedFiles.flatMap { this.getExposedStaticMembers(it) } +
        this.exposedClasses.flatMap { this.getExposedClassMembers(it) + this.getExposedConstructors(it) }) +
        this.exposedCategoryMembers
