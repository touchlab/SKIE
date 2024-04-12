package co.touchlab.skie.phases.apinotes.builder

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.oir.element.OirFunction
import co.touchlab.skie.oir.element.OirProperty
import co.touchlab.skie.oir.element.OirScope
import co.touchlab.skie.oir.element.OirValueParameter
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirVisibility
import co.touchlab.skie.sir.element.isAccessibleFromOtherModules

class ApiNotesFactory(
    private val exposeInternalMembers: Boolean,
) {

    context(SirPhase.Context)
    fun create(): ApiNotes =
        ApiNotes(
            moduleName = framework.moduleName,
            classes = oirProvider.kotlinClasses.map { it.toApiNote() },
            protocols = oirProvider.kotlinProtocols.map { it.toApiNote() },
        )

    context(SirPhase.Context)
    private fun OirClass.toApiNote(): ApiNotesType =
        ApiNotesType(
            objCFqName = this.name,
            bridgeFqName = this.bridgedSirClass?.fqName?.toLocalString(),
            swiftFqName = this.originalSirClass.publicName.toLocalString(),
            isHidden = this.originalSirClass.visibility.isHiddenInApiNotes,
            availability = this.originalSirClass.visibility.availability,
            methods = this.callableDeclarationsIncludingExtensions.filterIsInstance<OirFunction>().map { it.toApiNote() },
            properties = this.callableDeclarationsIncludingExtensions.filterIsInstance<OirProperty>().map { it.toApiNote() },
        )

    context(SirPhase.Context)
    private fun OirFunction.toApiNote(): ApiNotesMethod =
        ApiNotesMethod(
            objCSelector = this.selector,
            kind = this.scope.toMemberKind(),
            swiftName = this.originalSirCallableDeclaration.name,
            isHidden = this.originalSirCallableDeclaration.visibility.isHiddenInApiNotes,
            availability = this.originalSirCallableDeclaration.visibility.availability,
            resultType = this.returnType?.render() ?: "",
            // ErrorOut parameters are required for the header, but not strictly required in api notes
            // If put in ApiNotes with current implementation it results in an error: pointer to non-const type 'NSError * _Nullable' with no explicit ownership
            // For yet unknown reason the same type compiles if put in the Kotlin header.
            // This is possible to fix by explicitly stating the ownership, but it's not worth the effort right now.
            parameters = this.valueParameters.filter { it.originalSirValueParameter != null }.map { it.toApiNote() },
        )

    context(SirPhase.Context)
    private fun OirValueParameter.toApiNote(): ApiNotesParameter =
        ApiNotesParameter(
            position = this.index,
            type = this.type.render(),
        )

    context(SirPhase.Context)
    private fun OirProperty.toApiNote(): ApiNotesProperty =
        ApiNotesProperty(
            objCName = this.name,
            kind = this.scope.toMemberKind(),
            swiftName = this.originalSirProperty.name,
            isHidden = this.originalSirProperty.visibility.isHiddenInApiNotes,
            availability = this.originalSirProperty.visibility.availability,
            type = this.type.render(),
        )

    private val SirVisibility.isHiddenInApiNotes: Boolean
        get() = when (this) {
            SirVisibility.PublicButHidden -> true
            SirVisibility.PublicButReplaced -> true
            SirVisibility.Internal -> exposeInternalMembers
            else -> false
        }

    private val SirVisibility.availability: ApiNotesAvailabilityMode
        get() = when {
            this.isAccessibleFromOtherModules -> ApiNotesAvailabilityMode.Available
            this == SirVisibility.Internal && exposeInternalMembers -> ApiNotesAvailabilityMode.Available
            else -> ApiNotesAvailabilityMode.NonSwift
        }

    private fun OirScope.toMemberKind(): ApiNotesTypeMemberKind =
        when (this) {
            OirScope.Member -> ApiNotesTypeMemberKind.Instance
            OirScope.Static -> ApiNotesTypeMemberKind.Class
        }
}
