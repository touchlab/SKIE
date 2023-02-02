package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridgeParameter

class AsyncKotlinFunctionSwiftModel(
    private val delegate: MutableKotlinFunctionSwiftModel,
    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinFunctionSwiftModel by delegate {

    private val delegateCore = requireNotNull((delegate as? ActualKotlinFunctionSwiftModel)?.core) {
        "Delegate must be an instance of ActualKotlinFunctionSwiftModel to access its `core`'s parameter bridge. Was: $delegate"
    }

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = delegate.directlyCallableMembers

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel>
        get() = delegate.valueParameters.filter { it.origin == KotlinValueParameterSwiftModel.Origin.SuspendCompletion }

    override val returnType: TypeSwiftModel
        get() = with(swiftModelScope) {
            descriptor.asyncReturnTypeModel(
                receiver.swiftGenericExportScope,
                delegateCore.methodBridge.paramBridges.firstNotNullOf { it as? MethodBridgeParameter.ValueParameter.SuspendCompletion }
            )
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
