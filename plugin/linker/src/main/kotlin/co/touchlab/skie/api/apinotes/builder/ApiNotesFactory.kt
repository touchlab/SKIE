package co.touchlab.skie.api.apinotes.builder

import co.touchlab.skie.api.apinotes.fixes.fqNameSafeForBridging
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.name
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.name
import co.touchlab.skie.plugin.api.model.isHidden
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.descriptors.isInterface

internal class ApiNotesFactory(
    private val moduleName: String,
    private val descriptorProvider: DescriptorProvider,
    private val swiftModelScope: SwiftModelScope,
) {

    fun create(): ApiNotes = with(swiftModelScope) {
        ApiNotes(
            moduleName = moduleName,
            classes = descriptorProvider.swiftModelsForClassesAndFiles.map { it.toApiNote() },
            protocols = descriptorProvider.swiftModelsForInterfaces.map { it.toApiNote() },
        )
    }

    context(SwiftModelScope)
    private val DescriptorProvider.swiftModelsForClassesAndFiles: List<KotlinTypeSwiftModel>
        get() = this.classDescriptors.filterNot { it.kind.isInterface }.map { it.swiftModel } + this.exportedFiles.map { it.swiftModel }

    context(SwiftModelScope)
    private val DescriptorProvider.swiftModelsForInterfaces: List<KotlinTypeSwiftModel>
        get() = this.classDescriptors.filter { it.kind.isInterface }.map { it.swiftModel }

    context(SwiftModelScope)
    private fun KotlinTypeSwiftModel.toApiNote(): ApiNotesType =
        ApiNotesType(
            objCFqName = this.objCFqName,
            bridgeFqName = this.bridge?.fqNameSafeForBridging,
            swiftFqName = this.fqName,
            isHidden = this.visibility.isHiddenOrReplaced,
            isRemoved = this.visibility.isRemoved,
            methods = this.getApiNoteMethods(),
            properties = this.getApiNoteProperties(),
        )

    context(SwiftModelScope)
    private fun KotlinTypeSwiftModel.getApiNoteMethods(): List<ApiNotesMethod> {
        val callableMembers = descriptorProvider.getAllExposedBaseCallableMembers(this.descriptorHolder).map { it.swiftModel }

        val functions = callableMembers.filterIsInstance<KotlinFunctionSwiftModel>()

        val convertedPropertiesFunctions = callableMembers.filterIsInstance<KotlinConvertedPropertySwiftModel>().flatMap { it.accessors }

        return (functions + convertedPropertiesFunctions).map { it.toApiNote() }
    }

    context(SwiftModelScope)
    private fun KotlinTypeSwiftModel.getApiNoteProperties(): List<ApiNotesProperty> =
        descriptorProvider.getAllExposedBaseCallableMembers(this.descriptorHolder)
            .map { it.swiftModel }
            .filterIsInstance<KotlinRegularPropertySwiftModel>()
            .map { it.toApiNote() }

    private fun KotlinFunctionSwiftModel.toApiNote(): ApiNotesMethod =
        ApiNotesMethod(
            objCSelector = this.objCSelector,
            kind = this.receiver.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            isRemoved = this.visibility.isRemoved,
        )

    private fun KotlinRegularPropertySwiftModel.toApiNote(): ApiNotesProperty =
        ApiNotesProperty(
            objCName = this.objCName,
            kind = this.receiver.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            isRemoved = this.visibility.isRemoved,
        )

    private val SwiftModelVisibility.isHiddenOrReplaced: Boolean
        get() = this.isHidden || this.isReplaced

    private fun KotlinTypeSwiftModel.Kind.toMemberKind(): ApiNotesTypeMemberKind =
        when (this) {
            KotlinTypeSwiftModel.Kind.Class -> ApiNotesTypeMemberKind.Instance
            KotlinTypeSwiftModel.Kind.File -> ApiNotesTypeMemberKind.Class
        }

    private fun DescriptorProvider.getAllExposedBaseCallableMembers(
        containingDescriptorHolder: ClassOrFileDescriptorHolder,
    ): Collection<CallableMemberDescriptor> =
        when (containingDescriptorHolder) {
            is ClassOrFileDescriptorHolder.Class -> this.getAllExposedBaseCallableMembers(containingDescriptorHolder.value)
            is ClassOrFileDescriptorHolder.File -> this.getAllExposedBaseCallableMembers(containingDescriptorHolder.value)
        }

    private fun DescriptorProvider.getAllExposedBaseCallableMembers(
        classDescriptor: ClassDescriptor,
    ): Collection<CallableMemberDescriptor> =
        this.getFirstBaseMethodForAllExposedMethods(classDescriptor) +
            this.getFirstBasePropertyForAllExposedProperties(classDescriptor) +
            this.getExposedConstructors(classDescriptor) +
            this.getExposedCategoryMembers(classDescriptor)

    private fun DescriptorProvider.getAllExposedBaseCallableMembers(file: SourceFile): Collection<CallableMemberDescriptor> =
        this.getExposedFileContent(file)
}
