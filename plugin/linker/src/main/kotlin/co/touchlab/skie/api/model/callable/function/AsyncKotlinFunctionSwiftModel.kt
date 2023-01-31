package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.KotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import co.touchlab.skie.plugin.api.model.type.bridge.MethodBridgeParameter

class AsyncKotlinFunctionSwiftModel(
    private val delegate: MutableKotlinFunctionSwiftModel,
    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinFunctionSwiftModel by delegate {

    private val delegateCore = requireNotNull((delegate as? ActualKotlinFunctionSwiftModel)?.core) {
        "Delegate must be an instance of ActualKotlinFunctionSwiftModel to access its `core`'s parameter bridge"
    }

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = delegate.directlyCallableMembers

    override val parameters: List<MutableKotlinParameterSwiftModel>
        get() = delegate.parameters.filter { it.origin == KotlinParameterSwiftModel.Origin.SuspendCompletion }

    override val returnType: TypeSwiftModel
        get() = with(swiftModelScope) {
            descriptor.asyncReturnTypeModel(
                receiver.swiftGenericExportScope,
                delegateCore.methodBridge.paramBridges.firstNotNullOf { it as? MethodBridgeParameter.ValueParameter.SuspendCompletion }
            )
        }
}
