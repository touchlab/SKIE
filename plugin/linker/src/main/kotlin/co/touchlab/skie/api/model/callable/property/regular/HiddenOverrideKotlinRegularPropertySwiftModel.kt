package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class HiddenOverrideKotlinRegularPropertySwiftModel(
    private val baseModel: MutableKotlinRegularPropertySwiftModel,
    receiverDescriptor: ClassDescriptor,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinRegularPropertySwiftModel by baseModel {

    override val directlyCallableMembers: List<MutableKotlinRegularPropertySwiftModel> = listOf(this)

    override val receiver: TypeSwiftModel by lazy {
        with(swiftModelScope) {
            receiverDescriptor.swiftModel
        }
    }

    override val original: KotlinRegularPropertySwiftModel = OriginalKotlinRegularPropertySwiftModel(this)

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
