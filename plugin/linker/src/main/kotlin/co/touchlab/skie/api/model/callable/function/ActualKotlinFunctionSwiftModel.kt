package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.api.model.callable.parameter.ActualKotlinValueParameterSwiftModel
import co.touchlab.skie.api.model.callable.swiftModelOrigin
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor

internal class ActualKotlinFunctionSwiftModel(
    override val descriptor: FunctionDescriptor,
    override val allBoundedSwiftModels: List<MutableKotlinFunctionSwiftModel>,
    val core: KotlinFunctionSwiftModelCore,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinFunctionSwiftModel {

    override var identifier: String by core::identifier

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel> by lazy {
        core.getParameterCoresWithDescriptors(descriptor).map { (core, parameterDescriptor) ->
            ActualKotlinValueParameterSwiftModel(
                core,
                parameterDescriptor,
            ) {
                with(swiftModelScope) {
                    descriptor.getParameterType(parameterDescriptor, core.parameterBridge, receiver.swiftGenericExportScope)
                }
            }
        }
    }

    override var visibility: SwiftModelVisibility by core::visibility

    override val receiver: TypeSwiftModel by lazy {
        with(swiftModelScope) {
            descriptor.receiverTypeModel()
        }
    }

    override val objCSelector: String by core::objCSelector

    override val isSuspend: Boolean = descriptor.isSuspend

    override val isThrowing: Boolean by core::isThrowing

    override val role: KotlinFunctionSwiftModel.Role
        get() = when (descriptor) {
            is ConstructorDescriptor -> KotlinFunctionSwiftModel.Role.Constructor
            is PropertyGetterDescriptor -> KotlinFunctionSwiftModel.Role.ConvertedGetter
            is PropertySetterDescriptor -> KotlinFunctionSwiftModel.Role.ConvertedSetter
            else -> KotlinFunctionSwiftModel.Role.SimpleFunction
        }

    override val origin: KotlinCallableMemberSwiftModel.Origin = descriptor.swiftModelOrigin

    override var collisionResolutionStrategy: CollisionResolutionStrategy = CollisionResolutionStrategy.Rename

    override val original: KotlinFunctionSwiftModel = OriginalKotlinFunctionSwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility || valueParameters.any { it.isChanged } ||
            collisionResolutionStrategy != original.collisionResolutionStrategy

    override val returnType: TypeSwiftModel
        get() = with(swiftModelScope) {
            core.descriptor.returnTypeModel(receiver.swiftGenericExportScope, core.methodBridge.returnBridge)
        }

    override fun toString(): String = descriptor.toString()

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: KotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinDirectlyCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
