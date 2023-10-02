package co.touchlab.skie.phases.apinotes.builder

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.swiftmodel.isHidden
import co.touchlab.skie.swiftmodel.isRemoved
import co.touchlab.skie.swiftmodel.isReplaced
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.isInterface

object ApiNotesFactory {

    context(SirPhase.Context)
    fun create(): ApiNotes =
        ApiNotes(
            moduleName = framework.moduleName,
            classes = descriptorProvider.swiftModelsForClassesAndFiles.map { it.toApiNote() },
            protocols = descriptorProvider.swiftModelsForInterfaces.map { it.toApiNote() },
        )

    context(SirPhase.Context)
    private val DescriptorProvider.swiftModelsForClassesAndFiles: List<KotlinTypeSwiftModel>
        get() = this.exposedClasses.filterNot { it.kind.isInterface }.map { it.swiftModel } +
                this.exposedFiles.map { it.swiftModel }

    context(SirPhase.Context)
    private val DescriptorProvider.swiftModelsForInterfaces: List<KotlinTypeSwiftModel>
        get() = this.exposedClasses.filter { it.kind.isInterface }.map { it.swiftModel }

    context(SirPhase.Context)
    private fun KotlinTypeSwiftModel.toApiNote(): ApiNotesType =
        ApiNotesType(
            objCFqName = this.objCFqName.asString(),
            bridgeFqName = this.bridgedSirClass?.fqName?.toLocalString(),
            swiftFqName = this.kotlinSirClass.fqName.toLocalString(),
            isHidden = this.visibility.isHiddenOrReplaced,
            availability = this.visibility.availability,
            methods = this.allDirectlyCallableMembers.filterIsInstance<KotlinFunctionSwiftModel>().map { it.toApiNote(this) },
            properties = this.allDirectlyCallableMembers.filterIsInstance<KotlinRegularPropertySwiftModel>().map { it.toApiNote(this) },
        )

    context(SirPhase.Context)
    private fun KotlinFunctionSwiftModel.toApiNote(owner: KotlinTypeSwiftModel): ApiNotesMethod =
        ApiNotesMethod(
            objCSelector = this.objCSelector,
            kind = owner.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            availability = this.visibility.availability,
            resultType = this.objCReturnType?.let { objCTypeRenderer.render(it, this.reservedIdentifierInApiNotes) } ?: "",
            parameters = this.valueParameters.map { it.toApiNote(this) },
        )

    context(SirPhase.Context)
    private fun KotlinValueParameterSwiftModel.toApiNote(owner: KotlinFunctionSwiftModel): ApiNotesParameter =
        ApiNotesParameter(
            position = this.position,
            type = objCTypeRenderer.render(this.objCType, owner.reservedIdentifierInApiNotes),
        )

    context(SirPhase.Context)
    private fun KotlinRegularPropertySwiftModel.toApiNote(owner: KotlinTypeSwiftModel): ApiNotesProperty =
        ApiNotesProperty(
            objCName = this.objCName,
            kind = owner.kind.toMemberKind(),
            swiftName = this.name,
            isHidden = this.visibility.isHiddenOrReplaced,
            availability = this.visibility.availability,
            type = objCTypeRenderer.render(this.objCType, emptyList()),
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

private val KotlinFunctionSwiftModel.reservedIdentifierInApiNotes: List<String>
    get() = valueParameters.map { it.parameterName }
