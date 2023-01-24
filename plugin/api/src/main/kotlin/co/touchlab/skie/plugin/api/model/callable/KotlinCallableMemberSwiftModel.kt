package co.touchlab.skie.plugin.api.model.callable

import co.touchlab.skie.plugin.api.model.type.KotlinTypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor

interface KotlinCallableMemberSwiftModel {

    val descriptor: CallableMemberDescriptor

    val receiver: TypeSwiftModel

    val allBoundedSwiftModels: List<KotlinCallableMemberSwiftModel>

    fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT
}
