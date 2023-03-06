package co.touchlab.skie.api.model.callable.function

import co.touchlab.skie.api.model.callable.parameter.ActualKotlinValueParameterSwiftModel
import co.touchlab.skie.api.model.callable.swiftModelOrigin
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.plugin.api.model.type.FlowMappingStrategy
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor

internal class ActualKotlinFunctionSwiftModel(
    override val descriptor: FunctionDescriptor,
    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModelWithCore>,
    override val core: KotlinFunctionSwiftModelCore,
    private val swiftModelScope: MutableSwiftModelScope,
    descriptorProvider: DescriptorProvider,
) : KotlinFunctionSwiftModelWithCore {

    override var identifier: String by core::identifier

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel> by lazy {
        core.getParameterCoresWithDescriptors(descriptor).mapIndexed { index, (core, parameterDescriptor) ->
            ActualKotlinValueParameterSwiftModel(
                core,
                descriptor,
                parameterDescriptor,
                index,
            ) { isFlowMappingEnabled ->
                with(swiftModelScope) {
                    descriptor.getParameterType(
                        parameterDescriptor,
                        core.parameterBridge,
                        receiver.swiftGenericExportScope,
                        isFlowMappingEnabled,
                    )
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

    override val reference: String
        get() = core.reference(this)

    override val name: String
        get() = core.name(this)

    override val role: KotlinFunctionSwiftModel.Role
        get() = when (descriptor) {
            is ConstructorDescriptor -> KotlinFunctionSwiftModel.Role.Constructor
            is PropertyGetterDescriptor -> KotlinFunctionSwiftModel.Role.ConvertedGetter
            is PropertySetterDescriptor -> KotlinFunctionSwiftModel.Role.ConvertedSetter
            else -> KotlinFunctionSwiftModel.Role.SimpleFunction
        }

    override val scope: KotlinCallableMemberSwiftModel.Scope =
        if (descriptorProvider.getReceiverClassDescriptorOrNull(descriptor) == null) {
            KotlinCallableMemberSwiftModel.Scope.Static
        } else {
            KotlinCallableMemberSwiftModel.Scope.Member
        }

    override val origin: KotlinCallableMemberSwiftModel.Origin = descriptor.swiftModelOrigin

    override var collisionResolutionStrategy: CollisionResolutionStrategy = CollisionResolutionStrategy.Rename

    override val original: KotlinFunctionSwiftModel = OriginalKotlinFunctionSwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility || valueParameters.any { it.isChanged } ||
            collisionResolutionStrategy != original.collisionResolutionStrategy || returnTypeFlowMappingStrategy != original.returnTypeFlowMappingStrategy

    override val returnType: TypeSwiftModel
        get() = with(swiftModelScope) {
            core.descriptor.returnTypeModel(
                receiver.swiftGenericExportScope,
                core.getMethodBridge(descriptor).returnBridge,
                returnTypeFlowMappingStrategy,
            )
        }

    override var returnTypeFlowMappingStrategy: FlowMappingStrategy = FlowMappingStrategy.None

    override val objCReturnType: ObjCType?
        get() = core.getObjCReturnType(descriptor, returnTypeFlowMappingStrategy)

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
