package co.touchlab.skie.plugin.api.model.type

import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.sir.element.SirClass

interface MutableKotlinTypeSwiftModel : KotlinTypeSwiftModel {

    override var visibility: SwiftModelVisibility

    override val allAccessibleDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override val allDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override var bridgedSirClass: SirClass?
}
