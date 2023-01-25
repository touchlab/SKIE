@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.factory

import co.touchlab.skie.api.model.DescriptorBridgeProvider
import co.touchlab.skie.api.model.callable.function.ActualKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.callable.function.AsyncKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.callable.function.KotlinFunctionSwiftModelCore
import co.touchlab.skie.api.model.callable.property.converted.ActualKotlinConvertedPropertySwiftModel
import co.touchlab.skie.api.model.callable.property.regular.ActualKotlinRegularPropertySwiftModel
import co.touchlab.skie.api.model.callable.property.regular.KotlinRegularPropertySwiftModelCore
import co.touchlab.skie.api.model.type.classes.ActualKotlinClassSwiftModel
import co.touchlab.skie.api.model.type.enumentry.ActualKotlinEnumEntrySwiftModel
import co.touchlab.skie.api.model.type.files.ActualKotlinFileSwiftModel
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import co.touchlab.skie.plugin.reflection.reflectors.mapper
import org.jetbrains.kotlin.backend.konan.descriptors.enumEntries
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseMethod
import org.jetbrains.kotlin.backend.konan.objcexport.isBaseProperty
import org.jetbrains.kotlin.backend.konan.objcexport.isObjCProperty
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isEnumClass

class SwiftModelFactory(
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
    private val namer: ObjCExportNamer,
    private val bridgeProvider: DescriptorBridgeProvider,
) {

    fun createMembers(descriptors: List<CallableMemberDescriptor>): Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel> {
        val disjointSet = MultiRootDisjointSet<CallableMemberDescriptor> { descriptor ->
            descriptor.overriddenDescriptors.map { it.original }
        }

        disjointSet.addAll(descriptors.map { it.original })

        return disjointSet.sets.map { createBoundedMembers(it) }.fold(emptyMap()) { acc, element -> acc + element }
    }

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
        val allBoundedSwiftModels = mutableListOf<MutableKotlinFunctionSwiftModel>()

        val core = KotlinFunctionSwiftModelCore(group.representative, namer, bridgeProvider)

        return group
            .associateWith { ActualKotlinFunctionSwiftModel(it, allBoundedSwiftModels, core, swiftModelScope) }
            .also { allBoundedSwiftModels.addAll(it.values) }
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
        val allBoundedSwiftModels = mutableListOf<MutableKotlinCallableMemberSwiftModel>()

        val core = KotlinRegularPropertySwiftModelCore(group.representative, namer)

        return group
            .associateWith { ActualKotlinRegularPropertySwiftModel(it, allBoundedSwiftModels, core, swiftModelScope) }
            .also { allBoundedSwiftModels.addAll(it.values) }
            .mapKeys {
                @Suppress("USELESS_CAST")
                it.key as CallableMemberDescriptor
            }
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

    fun createClasses(descriptors: List<ClassDescriptor>): Map<ClassDescriptor, MutableKotlinClassSwiftModel> =
        descriptors
            .map { it.original }
            .associateWith { classDescriptor ->
                ActualKotlinClassSwiftModel(
                    classDescriptor = classDescriptor,
                    namer = namer,
                    swiftModelScope = swiftModelScope,
                    descriptorProvider = descriptorProvider,
                )
            }

    fun createEnumEntries(descriptors: List<ClassDescriptor>): Map<ClassDescriptor, KotlinEnumEntrySwiftModel> =
        descriptors
            .map { it.original }
            .filter { it.kind.isEnumClass }
            .flatMap { it.enumEntries }
            .associateWith { ActualKotlinEnumEntrySwiftModel(it, namer) }

    fun createFiles(files: List<SourceFile>): Map<SourceFile, MutableKotlinTypeSwiftModel> =
        files.associateWith { file ->
            ActualKotlinFileSwiftModel(
                file = file,
                module = descriptorProvider.getFileModule(file),
                namer = namer,
            )
        }

    fun createAsyncFunctions(models: Collection<MutableKotlinFunctionSwiftModel>): Map<FunctionDescriptor, MutableKotlinFunctionSwiftModel> =
        models.filter { it.descriptor.isSuspend }
            .map { it.allBoundedSwiftModels.toSet() }
            .distinct()
            .flatMap { group ->
                val allBoundedSwiftModels = mutableListOf<MutableKotlinFunctionSwiftModel>()

                group
                    .map { AsyncKotlinFunctionSwiftModel(it, allBoundedSwiftModels, swiftModelScope) }
                    .also { allBoundedSwiftModels.addAll(it) }
                    .map { it.descriptor to it }
            }
            .toMap()

}
