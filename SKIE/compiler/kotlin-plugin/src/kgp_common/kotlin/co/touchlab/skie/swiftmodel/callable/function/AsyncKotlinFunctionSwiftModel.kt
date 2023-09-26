package co.touchlab.skie.swiftmodel.callable.function

import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.parameter.KotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.swiftGenericExportScope
import co.touchlab.skie.swiftmodel.type.bridge.MethodBridgeParameter

class AsyncKotlinFunctionSwiftModel(
    private val delegate: KotlinFunctionSwiftModelWithCore,
    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinFunctionSwiftModel by delegate {

    override val directlyCallableMembers: List<MutableKotlinDirectlyCallableMemberSwiftModel>
        get() = delegate.directlyCallableMembers

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel>
        get() = delegate.valueParameters.filter { it.origin != KotlinValueParameterSwiftModel.Origin.SuspendCompletion }

    // TODO: This is a hack for calling async function wrappers needed until Sir supports functions.
    override val reference: String
        get() = delegate.core.replacedReference(this)

    override val name: String
        get() = delegate.core.name(this)

    override val isSuspend: Boolean = true

    override val isThrowing: Boolean = true

    override val returnType: SirType
        get() = with(swiftModelScope) {
            descriptor.asyncReturnType(
                swiftGenericExportScope,
                delegate.core.getMethodBridge(descriptor).paramBridges.firstNotNullOf { it as? MethodBridgeParameter.ValueParameter.SuspendCompletion },
                returnTypeFlowMappingStrategy,
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
