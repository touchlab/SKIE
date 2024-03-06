@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.utils.ResolvedDependency
import java.util.concurrent.atomic.AtomicReference

internal class NativeMutableDescriptorProvider(
    private val exposedModulesProvider: ExposedModulesProvider,
    private val config: KonanConfig,
    private val produceObjCExportInterface: () -> ObjCExportedInterface,
) : MutableDescriptorProvider {

    enum class State {
        MUTABLE,
        MUTATING,
        IMMUTABLE,
    }

    override val builtIns: KotlinBuiltIns
        get() = realProvider.builtIns

    override val extraDescriptorBuiltins: ExtraDescriptorBuiltins
        get() = realProvider.extraDescriptorBuiltins

    internal val objCExportedInterface: ObjCExportedInterface
        get() = realProvider.objCExportedInterface

    private lateinit var realProvider: NativeDescriptorProvider

    private lateinit var mutationScope: DescriptorRegistrationScope

    private val mutationListeners = mutableListOf<() -> Unit>()

    private val state = AtomicReference(State.MUTABLE)

    init {
        reload()
    }

    private fun reload() {
        realProvider = NativeDescriptorProvider(exposedModulesProvider, config, produceObjCExportInterface())

        mutationScope = object : DescriptorRegistrationScope, DescriptorProvider by realProvider {
            override fun registerExposedDescriptor(descriptor: DeclarationDescriptor) {
                realProvider.registerExposedDescriptor(descriptor)
            }
        }

        notifyMutationListeners()
    }

    override fun mutate(block: DescriptorRegistrationScope.() -> Unit) {
        if (!state.compareAndSet(State.MUTABLE, State.MUTATING)) {
            error("Cannot run mutate() while already mutating or immutable.")
        }
        try {
            mutationScope.block()
        } finally {
            notifyMutationListeners()
            if (!state.compareAndSet(State.MUTATING, State.MUTABLE)) {
                error("Expected state to be MUTATING, but was ${state.get()} (probably).")
            }
        }
    }

    override fun recalculateExports() {
        reload()
    }

    private fun notifyMutationListeners() {
        mutationListeners.forEach { it() }
    }

    fun finalize() {
        when (val witnessState = state.compareAndExchange(State.MUTABLE, State.IMMUTABLE)) {
            State.MUTABLE -> {
                // Create a fresh provider with all the descriptors we've seen so far.
                reload()
            }
            // We were already mutable, nothing to do.
            State.IMMUTABLE -> {}
            State.MUTATING -> error("Cannot finalize while mutating.")
            else -> error("Unexpected state: $witnessState")
        }
    }

    override fun onMutated(listener: () -> Unit) {
        mutationListeners.add(listener)
    }

    // MARK:- DescriptorProvider delegation
    override val exposedModules: Set<ModuleDescriptor> get() = realProvider.exposedModules
    override val exposedClasses: Set<ClassDescriptor> get() = realProvider.exposedClasses
    override val exposedFiles: Set<SourceFile> get() = realProvider.exposedFiles
    override val exposedCategoryMembers: Set<CallableMemberDescriptor> get() = realProvider.exposedCategoryMembers
    override val exposedTopLevelMembers: Set<CallableMemberDescriptor> get() = realProvider.exposedTopLevelMembers

    override val externalDependencies: Set<ResolvedDependency>
        get() = realProvider.externalDependencies

    override val buildInLibraries: Set<KotlinLibrary>
        get() = realProvider.buildInLibraries

    override val resolvedLibraries: List<KotlinLibrary>
        get() = realProvider.resolvedLibraries

    override val externalLibraries: Set<KotlinLibrary>
        get() = realProvider.externalLibraries

    override val localLibraries: Set<KotlinLibrary>
        get() = realProvider.localLibraries

    override fun isFromLocalModule(declarationDescriptor: DeclarationDescriptor): Boolean =
        realProvider.isFromLocalModule(declarationDescriptor)

    override fun isBaseMethod(functionDescriptor: FunctionDescriptor): Boolean =
        realProvider.isBaseMethod(functionDescriptor)

    override fun isBaseProperty(propertyDescriptor: PropertyDescriptor): Boolean =
        realProvider.isBaseProperty(propertyDescriptor)

    override fun isExposed(callableMemberDescriptor: CallableMemberDescriptor): Boolean =
        realProvider.isExposed(callableMemberDescriptor)

    override fun isExposable(callableMemberDescriptor: CallableMemberDescriptor): Boolean =
        realProvider.isExposable(callableMemberDescriptor)

    override fun isExposable(classDescriptor: ClassDescriptor): Boolean =
        realProvider.isExposable(classDescriptor)

    override fun getFileModule(file: SourceFile): ModuleDescriptor = realProvider.getFileModule(file)

    override fun getExposedClassMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
        realProvider.getExposedClassMembers(classDescriptor)

    override fun getExposedCategoryMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> =
        realProvider.getExposedCategoryMembers(classDescriptor)

    override fun getExposedConstructors(classDescriptor: ClassDescriptor): List<ClassConstructorDescriptor> =
        realProvider.getExposedConstructors(classDescriptor)

    override fun getExposedStaticMembers(file: SourceFile): List<CallableMemberDescriptor> =
        realProvider.getExposedStaticMembers(file)

    override fun getReceiverClassDescriptorOrNull(descriptor: CallableMemberDescriptor): ClassDescriptor? =
        realProvider.getReceiverClassDescriptorOrNull(descriptor)

    override fun getExposedCompanionObject(classDescriptor: ClassDescriptor): ClassDescriptor? =
        realProvider.getExposedCompanionObject(classDescriptor)

    override fun getExposedNestedClasses(classDescriptor: ClassDescriptor): List<ClassDescriptor> =
        realProvider.getExposedNestedClasses(classDescriptor)

    override fun getExposedEnumEntries(classDescriptor: ClassDescriptor): List<ClassDescriptor> =
        realProvider.getExposedEnumEntries(classDescriptor)
}
