@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.api.apinotes.builder

import co.touchlab.skie.api.apinotes.fixes.fqNameSafeForBridging
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.SwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.function.name
import co.touchlab.skie.plugin.api.model.isHidden
import co.touchlab.skie.plugin.api.model.isRemoved
import co.touchlab.skie.plugin.api.model.isReplaced
import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.extension.KotlinInterfaceExtensionPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.property.regular.name
import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.fqName
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapper

internal class ApiNotesFactory(
    private val moduleName: String,
    descriptorProvider: DescriptorProvider,
    mapper: ObjCExportMapper,
    private val swiftModelScope: SwiftModelScope,
) {

    private val descriptorProvider = HierarchicalDescriptorProvider(descriptorProvider, mapper)

    fun create(): ApiNotes = with(swiftModelScope) {
        ApiNotes(
            moduleName = moduleName,
            classes = descriptorProvider.swiftModelsForExportedClassesAndFiles.map { it.toApiNote() },
            protocols = descriptorProvider.exportedInterfaces.map { it.swiftModel.toApiNote() },
        )
    }

    context(SwiftModelScope)
    private val HierarchicalDescriptorProvider.swiftModelsForExportedClassesAndFiles: List<KotlinTypeSwiftModel>
        get() = (descriptorProvider.exportedClasses.map { it.swiftModel } + descriptorProvider.exportedFiles.map { it.swiftModel })

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
    private fun KotlinTypeSwiftModel.getApiNoteMethods(): List<ApiNotesMethod> =
        descriptorProvider.exportedBaseFunctions(this.descriptorHolder).map { it.swiftModel.toApiNote() } +
            getExportedBasePropertiesSwiftModel().filterIsInstance<KotlinInterfaceExtensionPropertySwiftModel>().flatMap { it.toApiNotes() }


    context(SwiftModelScope)
    private fun KotlinTypeSwiftModel.getApiNoteProperties(): List<ApiNotesProperty> =
        getExportedBasePropertiesSwiftModel().filterIsInstance<KotlinRegularPropertySwiftModel>().map { it.toApiNote() }

    context(SwiftModelScope)
    private fun KotlinTypeSwiftModel.getExportedBasePropertiesSwiftModel(): List<KotlinPropertySwiftModel> =
        descriptorProvider.exportedBaseProperties(this.descriptorHolder).map { it.swiftModel }

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

    private fun KotlinInterfaceExtensionPropertySwiftModel.toApiNotes(): List<ApiNotesMethod> =
        accessors.map { it.toApiNote() }

    private val SwiftModelVisibility.isHiddenOrReplaced: Boolean
        get() = this.isHidden || this.isReplaced

    private fun KotlinTypeSwiftModel.Kind.toMemberKind(): ApiNotesTypeMemberKind =
        when (this) {
            KotlinTypeSwiftModel.Kind.Class -> ApiNotesTypeMemberKind.Instance
            KotlinTypeSwiftModel.Kind.File -> ApiNotesTypeMemberKind.Class
        }
}
