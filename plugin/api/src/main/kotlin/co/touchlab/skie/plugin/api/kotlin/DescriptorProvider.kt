package co.touchlab.skie.plugin.api.kotlin

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile

interface DescriptorProvider {

    val transitivelyExposedClasses: List<ClassDescriptor>

    val exposedClasses: List<ClassDescriptor>

    val exposedFiles: List<SourceFile>

    val exposedCategoryMembers: List<CallableMemberDescriptor>

    val exposedTopLevelMembers: List<CallableMemberDescriptor>

    fun isExposed(descriptor: CallableMemberDescriptor): Boolean

    fun isExposed(descriptor: ClassDescriptor): Boolean

    fun registerExposedDescriptor(descriptor: DeclarationDescriptor)

    fun getFileModule(file: SourceFile): ModuleDescriptor

    /**
     * Functions/properties without extensions and constructors (including overridden ones)
     */
    fun getExposedClassMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor>

    /**
     * Functions/properties extensions for classes (not interfaces or generics)
     */
    fun getExposedCategoryMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor>

    fun getExposedConstructors(classDescriptor: ClassDescriptor): List<ConstructorDescriptor>

    fun getExposedStaticMembers(file: SourceFile): List<CallableMemberDescriptor>
}

fun DescriptorProvider.getAllExposedMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
    this.getExposedClassMembers(classDescriptor) +
        this.getExposedCategoryMembers(classDescriptor) +
        this.getExposedConstructors(classDescriptor)

val DescriptorProvider.allExposedMembers: List<CallableMemberDescriptor>
    get() = (this.exposedFiles.flatMap { this.getExposedStaticMembers(it) } +
        this.exposedClasses.flatMap { this.getAllExposedMembers(it) })
