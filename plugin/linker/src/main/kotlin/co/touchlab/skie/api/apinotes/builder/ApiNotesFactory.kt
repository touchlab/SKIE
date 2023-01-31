package co.touchlab.skie.api.apinotes.builder

import co.touchlab.skie.api.apinotes.fixes.fqNameSafeForBridging
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.getAllExposedMembers
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.isHidden
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.type.ClassOrFileDescriptorHolder
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
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
        get() = this.transitivelyExposedClasses.filterNot { it.kind.isInterface }.map { it.swiftModel } +
            this.exposedFiles.map { it.swiftModel }

    context(SwiftModelScope)
    private val DescriptorProvider.swiftModelsForInterfaces: List<KotlinTypeSwiftModel>
        get() = this.transitivelyExposedClasses.filter { it.kind.isInterface }.map { it.swiftModel }

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
        val callableMembers = descriptorProvider.getAllExposedMembers(this.descriptorHolder).map { it.swiftModel }

        val functions = callableMembers.filterIsInstance<KotlinFunctionSwiftModel>()

        val convertedPropertiesFunctions = callableMembers.filterIsInstance<KotlinConvertedPropertySwiftModel>().flatMap { it.accessors }

        return (functions + convertedPropertiesFunctions).mapNotNull { it.toApiNote() }
    }

    context(SwiftModelScope)
    private fun KotlinTypeSwiftModel.getApiNoteProperties(): List<ApiNotesProperty> =
        descriptorProvider.getAllExposedMembers(this.descriptorHolder)
            .map { it.swiftModel }
            .filterIsInstance<KotlinRegularPropertySwiftModel>()
            .mapNotNull { it.toApiNote() }

    private fun KotlinFunctionSwiftModel.toApiNote(): ApiNotesMethod? {
        return ApiNotesMethod(
            objCSelector = this.objCSelector,
            // TODO: What to do if the receiver isn't `KotlinTypeSwiftModel`?
            kind = (this.receiver as? KotlinTypeSwiftModel)?.kind?.toMemberKind() ?: return null,
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            isRemoved = this.visibility.isRemoved,
        )
    }

    private fun KotlinRegularPropertySwiftModel.toApiNote(): ApiNotesProperty? {
        return ApiNotesProperty(
            objCName = this.objCName,
            // TODO: What to do if the receiver isn't `KotlinTypeSwiftModel`?
            kind = (this.receiver as? KotlinTypeSwiftModel)?.kind?.toMemberKind() ?: return null,
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            isRemoved = this.visibility.isRemoved,
        )
    }

    private val SwiftModelVisibility.isHiddenOrReplaced: Boolean
        get() = this.isHidden || this.isReplaced

    private fun KotlinTypeSwiftModel.Kind.toMemberKind(): ApiNotesTypeMemberKind =
        when (this) {
            KotlinTypeSwiftModel.Kind.Class -> ApiNotesTypeMemberKind.Instance
            KotlinTypeSwiftModel.Kind.File -> ApiNotesTypeMemberKind.Class
        }

    private fun DescriptorProvider.getAllExposedMembers(
        containingDescriptorHolder: ClassOrFileDescriptorHolder,
    ): Collection<CallableMemberDescriptor> =
        when (containingDescriptorHolder) {
            is ClassOrFileDescriptorHolder.Class -> this.getAllExposedMembers(containingDescriptorHolder.value)
            is ClassOrFileDescriptorHolder.File -> this.getExposedStaticMembers(containingDescriptorHolder.value)
        }
}
