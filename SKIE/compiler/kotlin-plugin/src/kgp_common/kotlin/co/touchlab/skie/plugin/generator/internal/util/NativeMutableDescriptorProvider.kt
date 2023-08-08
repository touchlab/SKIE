@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.DescriptorRegistrationScope
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import co.touchlab.skie.plugin.reflection.reflectedBy
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import java.util.concurrent.atomic.AtomicReference
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

internal class NativeMutableDescriptorProvider(
    private val exposedModulesProvider: ExposedModulesProvider,
    private val config: KonanConfig,
    initialExportedInterface: ObjCExportedInterface,
): MutableDescriptorProvider, InternalDescriptorProvider {
    enum class State {
        MUTABLE,
        MUTATING,
        IMMUTABLE,
    }

    private lateinit var realProvider: NativeDescriptorProvider

    private lateinit var mutationScope: DescriptorRegistrationScope

    private val mutationListeners = mutableListOf<() -> Unit>()

    private val state = AtomicReference(State.MUTABLE)

    init {
        reload(initialExportedInterface)
    }

    fun reload(newExportedInterface: ObjCExportedInterface) {
        realProvider = NativeDescriptorProvider(exposedModulesProvider, config, newExportedInterface.reflectedBy())

        mutationScope = object: DescriptorRegistrationScope, DescriptorProvider by realProvider {
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

    private fun notifyMutationListeners() {
        mutationListeners.forEach { it() }
    }

    fun preventFurtherMutations(newExportedInterface: ObjCExportedInterface): InternalDescriptorProvider {
        when (val witnessState = state.compareAndExchange(State.MUTABLE, State.IMMUTABLE)) {
            State.MUTABLE -> {
                // Create a fresh provider with all the descriptors we've seen so far.
                reload(newExportedInterface)
            }
            // We were already mutable, nothing to do.
            State.IMMUTABLE -> {}
            State.MUTATING -> error("Cannot prevent further mutations while mutating.")
            null -> error("Unexpected state: $witnessState")
        }
        return realProvider
    }

    override fun onMutated(listener: () -> Unit) {
        mutationListeners.add(listener)
    }

    // MARK:- DescriptorProvider delegation
    override val mapper: ObjCExportMapper get() = realProvider.mapper
    override val exposedModules: Set<ModuleDescriptor> get() = realProvider.exposedModules
    override val exposedClasses: Set<ClassDescriptor> get() = realProvider.exposedClasses
    override val exposedFiles: Set<SourceFile> get() = realProvider.exposedFiles
    override val exposedCategoryMembers: Set<CallableMemberDescriptor> get() = realProvider.exposedCategoryMembers
    override val exposedTopLevelMembers: Set<CallableMemberDescriptor> get() = realProvider.exposedTopLevelMembers

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
