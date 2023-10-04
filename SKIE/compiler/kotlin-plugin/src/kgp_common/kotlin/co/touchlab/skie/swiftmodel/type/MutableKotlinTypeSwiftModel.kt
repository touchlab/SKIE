package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.sir.element.SirClass
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel

interface MutableKotlinTypeSwiftModel : KotlinTypeSwiftModel {

    override val allAccessibleDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override val allDirectlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>

    override var bridgedSirClass: SirClass?
}
