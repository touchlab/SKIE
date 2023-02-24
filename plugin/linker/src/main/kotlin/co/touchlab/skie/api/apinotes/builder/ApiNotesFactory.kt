package co.touchlab.skie.api.apinotes.builder

import co.touchlab.skie.api.apinotes.fixes.fqNameSafeForBridging
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.isHidden
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
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
        get() = this.exposedClasses.filterNot { it.kind.isInterface }.map { it.swiftModel } +
            this.exposedFiles.map { it.swiftModel }

    context(SwiftModelScope)
    private val DescriptorProvider.swiftModelsForInterfaces: List<KotlinTypeSwiftModel>
        get() = this.exposedClasses.filter { it.kind.isInterface }.map { it.swiftModel }

    context(SwiftModelScope)
    private fun KotlinTypeSwiftModel.toApiNote(): ApiNotesType =
        ApiNotesType(
            objCFqName = this.objCFqName,
            bridgeFqName = this.bridge?.fqNameSafeForBridging,
            swiftFqName = this.fqName,
            isHidden = this.visibility.isHiddenOrReplaced,
            availability = this.visibility.availability,
            methods = this.allDirectlyCallableMembers.filterIsInstance<KotlinFunctionSwiftModel>().map { it.toApiNote(this) },
            properties = this.allDirectlyCallableMembers.filterIsInstance<KotlinRegularPropertySwiftModel>().map { it.toApiNote(this) },
        )

    private fun KotlinFunctionSwiftModel.toApiNote(owner: KotlinTypeSwiftModel): ApiNotesMethod =
        ApiNotesMethod(
            objCSelector = this.objCSelector,
            kind = owner.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            availability = this.visibility.availability,
            resultType = this.objCReturnType?.renderWithExplicitNullability() ?: "",
            parameters = this.valueParameters.map { it.toApiNote() },
        )

    private fun KotlinValueParameterSwiftModel.toApiNote(): ApiNotesParameter =
        ApiNotesParameter(
            position = this.position,
            type = this.objCType.renderWithExplicitNullability(),
        )

    private fun KotlinRegularPropertySwiftModel.toApiNote(owner: KotlinTypeSwiftModel): ApiNotesProperty =
        ApiNotesProperty(
            objCName = this.objCName,
            kind = owner.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            availability = this.visibility.availability,
            type = this.objCType.renderWithExplicitNullability(),
        )

    private val SwiftModelVisibility.isHiddenOrReplaced: Boolean
        get() = this.isHidden || this.isReplaced

    private val SwiftModelVisibility.availability: ApiNotesAvailabilityMode
        get() = if (this.isRemoved) ApiNotesAvailabilityMode.NonSwift else ApiNotesAvailabilityMode.Available

    private fun KotlinTypeSwiftModel.Kind.toMemberKind(): ApiNotesTypeMemberKind =
        when (this) {
            KotlinTypeSwiftModel.Kind.Class, KotlinTypeSwiftModel.Kind.Interface -> ApiNotesTypeMemberKind.Instance
            KotlinTypeSwiftModel.Kind.File -> ApiNotesTypeMemberKind.Class
        }
}
