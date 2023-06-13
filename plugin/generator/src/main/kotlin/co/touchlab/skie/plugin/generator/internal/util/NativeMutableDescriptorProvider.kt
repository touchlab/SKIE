@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.DescriptorRegistrationScope
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import java.util.concurrent.atomic.AtomicReference

internal class NativeMutableDescriptorProvider(
    private val context: CommonBackendContext,
): MutableDescriptorProvider, InternalDescriptorProvider {
    enum class State {
        MUTABLE,
        MUTATING,
        IMMUTABLE,
    }

    private var realProvider = NativeDescriptorProvider(context)

    private val mutationListeners = mutableListOf<() -> Unit>()
    private val mutationScope = object: DescriptorRegistrationScope, DescriptorProvider by realProvider {
        override fun registerExposedDescriptor(descriptor: DeclarationDescriptor) {
            realProvider.registerExposedDescriptor(descriptor)
        }
    }
    private val state = AtomicReference(State.MUTABLE)

    override fun mutate(block: DescriptorRegistrationScope.() -> Unit) {
        if (!state.compareAndSet(State.MUTABLE, State.MUTATING)) {
            error("Cannot run mutate() while already mutating or immutable.")
        }
        try {
            mutationScope.block()
        } finally {
            mutationListeners.forEach { it() }
            if (!state.compareAndSet(State.MUTATING, State.MUTABLE)) {
                error("Expected state to be MUTATING, but was ${state.get()} (probably).")
            }
        }
    }

    override fun preventFurtherMutations(): InternalDescriptorProvider {
        when (val witnessState = state.compareAndExchange(State.MUTABLE, State.IMMUTABLE)) {
            // No more mutations can occur, so we can clear listeners.
            State.MUTABLE -> {
                mutationListeners.clear()
                // Create a fresh provider with all the descriptors we've seen so far.
                realProvider = NativeDescriptorProvider(context)
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
