package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.sir.element.SirClass

interface MutableKotlinTypeSwiftModel : KotlinTypeSwiftModel {

    override var visibility: SwiftModelVisibility

    override val allAccessibleDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override val allDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override var bridgedSirClass: SirClass?
}
