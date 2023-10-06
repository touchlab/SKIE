package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModel

interface MutableKotlinFileSwiftModel : KotlinFileSwiftModel, MutableKotlinTypeSwiftModel {

    override val allCallableMembers: List<MutableKotlinCallableMemberSwiftModel>
}
