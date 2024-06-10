@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.UserVisibleIrModulesSupportReflector
import co.touchlab.skie.kir.descriptor.cache.CachedObjCExportMapper
import co.touchlab.skie.kir.descriptor.cache.ExposedDescriptorsCache
import org.jetbrains.kotlin.backend.konan.FrontendServices
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.backend.konan.ir.konanLibrary
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyAccessorDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered
import org.jetbrains.kotlin.utils.ResolvedDependency

internal class NativeDescriptorProvider(
    override val exposedModules: Set<ModuleDescriptor>,
    private val konanConfig: KonanConfig,
    frontendServices: FrontendServices,
) : MutableDescriptorProvider {

    override val mapper = CachedObjCExportMapper(konanConfig, frontendServices)

    override val builtIns: KotlinBuiltIns = exposedModules.first().builtIns

    private val exposedDescriptorsCache = ExposedDescriptorsCache(
        mapper = mapper,
        builtIns = builtIns,
        objcGenerics = konanConfig.configuration.getBoolean(KonanConfigKeys.OBJC_GENERICS),
    ).also {
        it.exposeModules(exposedModules)
    }

    override val extraDescriptorBuiltins: ExtraDescriptorBuiltins = ExtraDescriptorBuiltins(exposedModules)

    override val exposedClasses: Set<ClassDescriptor>
        get() = exposedDescriptorsCache.exposedClasses

    override val exposedFiles: Set<SourceFile>
        get() = exposedDescriptorsCache.exposedTopLevelMembersByFile.keys

    override val exposedCategoryMembers: Set<CallableMemberDescriptor>
        get() = exposedDescriptorsCache.exposedCategoryMembers

    override val exposedTopLevelMembers: Set<CallableMemberDescriptor>
        get() = exposedDescriptorsCache.exposedTopLevelMembers

    override val externalDependencies: Set<ResolvedDependency> by lazy {
        konanConfig.userVisibleIrModulesSupport
            .reflectedBy<UserVisibleIrModulesSupportReflector>()
            .externalDependencyModules
            .toSet()
    }

    override val resolvedLibraries: List<KotlinLibrary> by lazy {
        konanConfig.resolvedLibraries.getFullList()
    }

    override val buildInLibraries: Set<KotlinLibrary> by lazy {
        resolvedLibraries.filter { it.isDefault }.toSet() +
        // Kotlin 2.0 changed how is the stdlib handled and for some reason it's no longer marked as default
            setOfNotNull(builtIns.any.module.konanLibrary)
    }

    override val externalLibraries: Set<KotlinLibrary> by lazy {
        val externalLibrariesArtifacts = externalDependencies.flatMap { it.artifactPaths }.map { it.path }.toSet()

        resolvedLibraries
            .filter { it.libraryFile.absolutePath in externalLibrariesArtifacts }
            .toSet() - buildInLibraries
    }

    override val localLibraries: Set<KotlinLibrary> by lazy {
        resolvedLibraries.toSet() - buildInLibraries - externalLibraries
    }

    override fun isFromLocalModule(declarationDescriptor: DeclarationDescriptor): Boolean =
        declarationDescriptor.module.konanLibrary in localLibraries

    override fun isExposed(callableMemberDescriptor: CallableMemberDescriptor): Boolean =
        callableMemberDescriptor.isExposed

    @get:JvmName("isExposedExtension")
    private val CallableMemberDescriptor.isExposed: Boolean
        get() = this in exposedTopLevelMembers ||
            this in exposedCategoryMembers ||
            (this.containingDeclaration in exposedClasses && mapper.shouldBeExposed(this))

    override fun getFileModule(file: SourceFile): ModuleDescriptor =
        exposedDescriptorsCache.exposedTopLevelMembersByFile[file]?.firstOrNull()?.module
            ?: error("File $file is not known to contain exported top level declarations.")

    override fun getExposedClassMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
        classDescriptor.unsubstitutedMemberScope
            .getDescriptorsFiltered(kindFilter = DescriptorKindFilter.CALLABLES)
            .filterIsInstance<CallableMemberDescriptor>()
            .filter { it.isExposed }

    override fun getExposedCategoryMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
        exposedDescriptorsCache.exposedCategoryMembersByClass[classDescriptor] ?: emptyList()

    override fun getExposedConstructors(classDescriptor: ClassDescriptor): List<ClassConstructorDescriptor> =
        classDescriptor.constructors.filter { it.isExposed }

    override fun getExposedStaticMembers(file: SourceFile): List<CallableMemberDescriptor> =
        exposedDescriptorsCache.exposedTopLevelMembersByFile[file] ?: emptyList()

    override fun getReceiverClassDescriptorOrNull(descriptor: CallableMemberDescriptor): ClassDescriptor? {
        val categoryClass = mapper.getClassIfCategory(descriptor)
        val containingDeclaration = descriptor.containingDeclaration

        return when {
            categoryClass != null -> categoryClass
            descriptor is PropertyAccessorDescriptor -> getReceiverClassDescriptorOrNull(descriptor.correspondingProperty)
            containingDeclaration is ClassDescriptor -> containingDeclaration
            else -> null
        }
    }

    override fun exposeCallableMember(callableDeclaration: CallableMemberDescriptor) {
        exposedDescriptorsCache.exposeAnyMember(callableDeclaration)
    }
}
