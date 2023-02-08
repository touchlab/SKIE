package co.touchlab.skie.api.apinotes.builder

import co.touchlab.skie.api.apinotes.fixes.fqNameSafeForBridging
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
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
            isRemoved = this.visibility.isRemoved,
            methods = this.allDirectlyCallableMembers.filterIsInstance<KotlinFunctionSwiftModel>().map { it.toApiNote(this) },
            properties = this.allDirectlyCallableMembers.filterIsInstance<KotlinRegularPropertySwiftModel>().map { it.toApiNote(this) },
        )

    private fun KotlinFunctionSwiftModel.toApiNote(owner: KotlinTypeSwiftModel): ApiNotesMethod {
        return ApiNotesMethod(
            objCSelector = this.objCSelector,
            kind = owner.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            isRemoved = this.visibility.isRemoved,
        )
    }

    private fun KotlinRegularPropertySwiftModel.toApiNote(owner: KotlinTypeSwiftModel): ApiNotesProperty {
        return ApiNotesProperty(
            objCName = this.objCName,
            kind = owner.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            isRemoved = this.visibility.isRemoved,
        )
    }

    private val SwiftModelVisibility.isHiddenOrReplaced: Boolean
        get() = this.isHidden || this.isReplaced

    private fun KotlinTypeSwiftModel.Kind.toMemberKind(): ApiNotesTypeMemberKind =
        when (this) {
            KotlinTypeSwiftModel.Kind.Class, KotlinTypeSwiftModel.Kind.Interface -> ApiNotesTypeMemberKind.Instance
            KotlinTypeSwiftModel.Kind.File -> ApiNotesTypeMemberKind.Class
        }
}
