package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.kir.descriptor.cache.CachedObjCExportMapper
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.utils.ResolvedDependency

interface DescriptorProvider {

    val mapper: CachedObjCExportMapper

    val builtIns: KotlinBuiltIns

    val extraDescriptorBuiltins: ExtraDescriptorBuiltins

    val exposedModules: Set<ModuleDescriptor>

    val exposedClasses: Set<ClassDescriptor>

    val exposedFiles: Set<SourceFile>

    val exposedCategoryMembers: Set<CallableMemberDescriptor>

    val exposedTopLevelMembers: Set<CallableMemberDescriptor>

    val externalDependencies: Set<ResolvedDependency>

    val buildInLibraries: Set<KotlinLibrary>

    val resolvedLibraries: List<KotlinLibrary>

    val externalLibraries: Set<KotlinLibrary>

    val localLibraries: Set<KotlinLibrary>

    fun isFromLocalModule(declarationDescriptor: DeclarationDescriptor): Boolean

    fun isExposed(callableMemberDescriptor: CallableMemberDescriptor): Boolean

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
}

fun DescriptorProvider.getAllExposedMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
    this.getExposedClassMembers(classDescriptor) +
        this.getExposedCategoryMembers(classDescriptor) +
        this.getExposedConstructors(classDescriptor)

val DescriptorProvider.allExposedMembers: Set<CallableMemberDescriptor>
    get() = this.exposedTopLevelMembers +
        this.exposedClasses.flatMap { this.getExposedClassMembers(it) + this.getExposedConstructors(it) } +
        this.exposedCategoryMembers
