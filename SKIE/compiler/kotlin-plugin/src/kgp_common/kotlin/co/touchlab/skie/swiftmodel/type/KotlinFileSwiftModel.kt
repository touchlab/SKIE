package co.touchlab.skie.swiftmodel.type

import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel

interface KotlinFileSwiftModel : KotlinTypeSwiftModel {

    val allCallableMembers: List<KotlinCallableMemberSwiftModel>
}
