@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.model.factory

import co.touchlab.skie.api.model.DescriptorBridgeProvider
import co.touchlab.skie.api.model.callable.function.AsyncKotlinFunctionSwiftModel
import co.touchlab.skie.api.model.callable.function.KotlinFunctionSwiftModelWithCore
import co.touchlab.skie.api.model.type.ActualKotlinClassSwiftModel
import co.touchlab.skie.api.model.type.ActualKotlinEnumEntrySwiftModel
import co.touchlab.skie.api.model.type.ActualKotlinFileSwiftModel
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinClassSwiftModel
import co.touchlab.skie.plugin.api.model.type.MutableKotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.enumentry.KotlinEnumEntrySwiftModel
import org.jetbrains.kotlin.backend.konan.descriptors.enumEntries
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isEnumClass

class SwiftModelFactory(
    private val swiftModelScope: MutableSwiftModelScope,
    private val descriptorProvider: DescriptorProvider,
    private val namer: ObjCExportNamer,
    bridgeProvider: DescriptorBridgeProvider,
) {

    private val membersDelegate = SwiftModelFactoryMembersDelegate(swiftModelScope, descriptorProvider, namer, bridgeProvider)

    fun createMembers(descriptors: List<CallableMemberDescriptor>): Map<CallableMemberDescriptor, MutableKotlinCallableMemberSwiftModel> =
        membersDelegate.createMembers(descriptors)

    fun createClasses(descriptors: Set<ClassDescriptor>): Map<ClassDescriptor, MutableKotlinClassSwiftModel> =
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

    fun createEnumEntries(descriptors: Set<ClassDescriptor>): Map<ClassDescriptor, KotlinEnumEntrySwiftModel> =
        descriptors
            .map { it.original }
            .filter { it.kind.isEnumClass }
            .flatMap { it.enumEntries }
            .associateWith { ActualKotlinEnumEntrySwiftModel(it, namer, swiftModelScope) }

    fun createFiles(files: Set<SourceFile>): Map<SourceFile, MutableKotlinTypeSwiftModel> =
        files.associateWith { file ->
            ActualKotlinFileSwiftModel(
                file = file,
                module = descriptorProvider.getFileModule(file),
                namer = namer,
                swiftModelScope = swiftModelScope,
                descriptorProvider = descriptorProvider,
            )
        }

    internal fun createAsyncFunctions(
        models: Collection<KotlinFunctionSwiftModelWithCore>,
    ): Map<FunctionDescriptor, MutableKotlinFunctionSwiftModel> =
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
