package co.touchlab.skie.swiftmodel.callable.function

import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.descriptors.ClassDescriptor

class HiddenOverrideKotlinFunctionSwiftModel(
    private val baseModel: KotlinFunctionSwiftModelWithCore,
    ownerDescriptor: ClassDescriptor,
    private val swiftModelScope: MutableSwiftModelScope,
) : KotlinFunctionSwiftModelWithCore by baseModel {

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel> = listOf(this)

    override val owner: KotlinTypeSwiftModel by lazy {
        with(swiftModelScope) {
            ownerDescriptor.swiftModel
        }
    }

    override val receiver: SirType by lazy {
        with(swiftModelScope) {
            ownerDescriptor.receiverType()
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
