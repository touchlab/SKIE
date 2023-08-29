@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.factory

import co.touchlab.skie.api.model.DescriptorBridgeProvider
import co.touchlab.skie.api.model.callable.function.ActualKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.callable.function.FakeObjcConstructorKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.callable.function.HiddenOverrideKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.callable.function.KotlinFunctionSwiftModelCore
import co.touchlab.skie.api.model.callable.function.KotlinFunctionSwiftModelWithCore
import co.touchlab.skie.api.model.callable.property.converted.ActualKotlinConvertedPropertySwiftModel
import co.touchlab.skie.api.model.callable.property.regular.ActualKotlinRegularPropertySwiftModel
import co.touchlab.skie.api.model.callable.property.regular.HiddenOverrideKotlinRegularPropertySwiftModel
import co.touchlab.skie.api.model.callable.property.regular.KotlinRegularPropertySwiftModelCore
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.backend.konan.objcexport.shouldBeExposed
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperclassesWithoutAny

class SwiftModelFactoryMembersDelegate(
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
    private val namer: ObjCExportNamer,
    private val bridgeProvider: DescriptorBridgeProvider,
) {

    private val exposedClassChildrenCache = ExposedClassChildrenCache(descriptorProvider)

    private val objCTypeProvider = ObjCTypeProvider(descriptorProvider, swiftModelScope, namer)

    fun createMembers(descriptors: List<CallableMemberDescriptor>): Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel> {
        val disjointSet = MultiRootDisjointSet(::getDirectParents)

        disjointSet.addAll(descriptors.map { it.original })

        return disjointSet.sets.map { createBoundedMembers(it) }.fold(emptyMap()) { acc, element -> acc + element }
    }

    private fun getDirectParents(descriptor: CallableMemberDescriptor): List<CallableMemberDescriptor> =
        if (descriptor is ConstructorDescriptor) getDirectParents(descriptor) else descriptor.overriddenDescriptors.map { it.original }
            .filter { namer.mapper.shouldBeExposed(it) }

    private fun getDirectParents(descriptor: ConstructorDescriptor): List<CallableMemberDescriptor> =
        descriptor.constructedClass
            .getAllSuperclassesWithoutAny()
            .flatMap { it.constructors }
            .filter { namer.getSelector(it) == namer.getSelector(descriptor) }

    @Suppress("UNCHECKED_CAST")
    private fun createBoundedMembers(
        group: Collection<CallableMemberDescriptor>,
    ): Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel> =
        when (group.first()) {
            is FunctionDescriptor -> createBoundedFunctions(group.map { it as FunctionDescriptor })
            is PropertyDescriptor -> createBoundedProperties(group.map { it as PropertyDescriptor })
            else -> throw IllegalArgumentException("Unknown group type: $group")
        } as Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel>

    private fun createBoundedFunctions(group: List<FunctionDescriptor>): Map<FunctionDescriptor, MutableKotlinFunctionSwiftModel> {
        val allBoundedSwiftModels = mutableListOf<KotlinFunctionSwiftModelWithCore>()

        val core = KotlinFunctionSwiftModelCore(group.representative, namer, bridgeProvider, objCTypeProvider)

        return group
            .associateWith { ActualKotlinFunctionSwiftModel(it, allBoundedSwiftModels, core, swiftModelScope, descriptorProvider) }
            .also { allBoundedSwiftModels.addAll(it.values) }
            .also { allBoundedSwiftModels.addFakeObjcConstructors(group, it.values.first()) }
            .also { allBoundedSwiftModels.addHiddenOverrides(group, it.values.first()) }
    }

    private fun MutableList<KotlinFunctionSwiftModelWithCore>.addFakeObjcConstructors(
        group: List<FunctionDescriptor>,
        representativeModel: KotlinFunctionSwiftModelWithCore,
    ) {
        if (representativeModel.role != KotlinFunctionSwiftModel.Role.Constructor) {
            return
        }

        val hiddenOverrides = group.getMissingChildClasses()
            .map { FakeObjcConstructorKotlinFunctionSwiftModel(representativeModel, it, swiftModelScope, objCTypeProvider) }

        this.addAll(hiddenOverrides)
    }

    private fun MutableList<KotlinFunctionSwiftModelWithCore>.addHiddenOverrides(
        group: List<FunctionDescriptor>,
        representativeModel: KotlinFunctionSwiftModelWithCore,
    ) {
        if (representativeModel.role == KotlinFunctionSwiftModel.Role.Constructor) {
            return
        }

        val hiddenOverrides = group.getMissingChildClasses()
            .map { HiddenOverrideKotlinFunctionSwiftModel(representativeModel, it, swiftModelScope) }

        this.addAll(hiddenOverrides)
    }

    private fun createBoundedProperties(group: List<PropertyDescriptor>): Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel> =
        if (namer.mapper.isObjCProperty(group.representative)) {
            createBoundedRegularProperties(group)
        } else {
            createBoundedConvertedProperties(group)
        }

    private fun createBoundedRegularProperties(
        group: List<PropertyDescriptor>,
    ): Map<CallableMemberDescriptor, MutableKotlinRegularPropertySwiftModel> {
        val allBoundedSwiftModels = mutableListOf<MutableKotlinDirectlyCallableMemberSwiftModel>()

        val core = KotlinRegularPropertySwiftModelCore(group.representative, namer, objCTypeProvider)

        return group
            .associateWith { ActualKotlinRegularPropertySwiftModel(it, allBoundedSwiftModels, core, swiftModelScope, descriptorProvider) }
            .also { allBoundedSwiftModels.addAll(it.values) }
            .also { allBoundedSwiftModels.addHiddenOverrides(group, it.values.first()) }
            .mapKeys {
                @Suppress("USELESS_CAST")
                it.key as CallableMemberDescriptor
            }
    }

    private fun MutableList<MutableKotlinDirectlyCallableMemberSwiftModel>.addHiddenOverrides(
        group: List<PropertyDescriptor>,
        representativeModel: MutableKotlinRegularPropertySwiftModel,
    ) {
        val hiddenOverrides = group.getMissingChildClasses()
            .map { HiddenOverrideKotlinRegularPropertySwiftModel(representativeModel, it, swiftModelScope) }

        this.addAll(hiddenOverrides)
    }

    private fun List<CallableMemberDescriptor>.getMissingChildClasses(): Set<ClassDescriptor> {
        val allBaseClasses = this.mapNotNull { descriptorProvider.getReceiverClassDescriptorOrNull(it) }
        val allChildrenClasses = allBaseClasses.flatMap { exposedClassChildrenCache.getExposedChildren(it) }

        return allChildrenClasses.toSet() - allBaseClasses.toSet()
    }

    private fun createBoundedConvertedProperties(
        group: List<PropertyDescriptor>,
    ): Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel> =
        createBoundedConvertedPropertiesEnclosingModels(group) +
                createBoundedConvertedPropertiesFunctions(group)

    private fun createBoundedConvertedPropertiesEnclosingModels(
        group: List<PropertyDescriptor>,
    ): Map<PropertyDescriptor, MutableKotlinCallableMemberSwiftModel> {
        val allBoundedSwiftModels = mutableListOf<MutableKotlinCallableMemberSwiftModel>()

        return group
            .associateWith { ActualKotlinConvertedPropertySwiftModel(it, allBoundedSwiftModels, swiftModelScope) }
            .also { allBoundedSwiftModels.addAll(it.values) }
    }

    private fun createBoundedConvertedPropertiesFunctions(
        group: List<PropertyDescriptor>,
    ): Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel> {
        val getters = createBoundedFunctions(group.mapNotNull { it.getter?.original })

        val setters = group.mapNotNull { it.setter?.original }.takeIf { it.isNotEmpty() }?.let { createBoundedFunctions(it) }

        return getters + (setters ?: emptyMap())
    }

    private val Collection<FunctionDescriptor>.representative: FunctionDescriptor
        get() = this.first { namer.mapper.isBaseMethod(it) }

    private val Collection<PropertyDescriptor>.representative: PropertyDescriptor
        get() = this.first { namer.mapper.isBaseProperty(it) }
}
