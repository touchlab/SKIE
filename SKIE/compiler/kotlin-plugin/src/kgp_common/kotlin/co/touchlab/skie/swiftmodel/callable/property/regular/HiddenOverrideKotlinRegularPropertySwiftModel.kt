package co.touchlab.skie.swiftmodel.callable.property.regular

import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class HiddenOverrideKotlinRegularPropertySwiftModel(
    private val baseModel: MutableKotlinRegularPropertySwiftModel,
    receiverDescriptor: ClassDescriptor,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinRegularPropertySwiftModel by baseModel {

    override val directlyCallableMembers: List<MutableKotlinRegularPropertySwiftModel> = listOf(this)

    override val receiver: SirType by lazy {
        with(swiftModelScope) {
            receiverDescriptor.receiverType()
        }
    }

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
