package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor

internal class HiddenOverrideKotlinFunctionSwiftModel(
    private val baseModel: KotlinFunctionSwiftModelWithCore,
    receiverDescriptor: ClassDescriptor,
    private val swiftModelScope: MutableSwiftModelScope,
) : KotlinFunctionSwiftModelWithCore by baseModel {

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel> = listOf(this)

    override val receiver: TypeSwiftModel by lazy {
        with(swiftModelScope) {
            receiverDescriptor.swiftModel
        }
    }

    override val original: KotlinFunctionSwiftModel = OriginalKotlinFunctionSwiftModel(this)

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
