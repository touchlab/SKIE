package co.touchlab.skie.swiftmodel.callable.function

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirConstructor
import co.touchlab.skie.sir.element.SirFunction
import co.touchlab.skie.sir.flowMappingStrategy
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SkieErrorSirType
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.parameter.ActualKotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.parameter.MutableKotlinValueParameterSwiftModel
import co.touchlab.skie.swiftmodel.callable.swiftGenericExportScope
import co.touchlab.skie.swiftmodel.callable.swiftModelOrigin
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyGetterDescriptor
import org.jetbrains.kotlin.descriptors.PropertySetterDescriptor

class ActualKotlinFunctionSwiftModel(
    override val descriptor: FunctionDescriptor,
    kotlinSirCallableDeclarationFactory: () -> SirCallableDeclaration,
    override val allBoundedSwiftModels: List<KotlinFunctionSwiftModelWithCore>,
    override val core: KotlinFunctionSwiftModelCore,
    private val swiftModelScope: MutableSwiftModelScope,
    descriptorProvider: DescriptorProvider,
    private val skieContext: SkiePhase.Context,
) : KotlinFunctionSwiftModelWithCore {

    override val kotlinSirCallableDeclaration: SirCallableDeclaration by lazy {
        kotlinSirCallableDeclarationFactory()
    }

    override val valueParameters: List<MutableKotlinValueParameterSwiftModel> by lazy {
        core.getParameterCoresWithDescriptors(descriptor).mapIndexed { index, (core, parameterDescriptor) ->
            ActualKotlinValueParameterSwiftModel(
                core,
                descriptor,
                parameterDescriptor,
                index,
                skieContext,
            ) { isFlowMappingEnabled ->
                with(swiftModelScope) {
                    descriptor.getParameterType(
                        parameterDescriptor,
                        core.parameterBridge,
                        swiftGenericExportScope,
                        isFlowMappingEnabled,
                    )
                }
            }
        }
    }

    override val kotlinSirFunction: SirFunction
        get() = kotlinSirCallableDeclaration as? SirFunction ?: error("Constructor $kotlinSirCallableDeclaration does not have a SirFunction.")

    override val kotlinSirConstructor: SirConstructor
        get() = kotlinSirCallableDeclaration as? SirConstructor ?: error("Function $kotlinSirCallableDeclaration does not have a SirConstructor.")

    override var bridgedSirCallableDeclaration: SirCallableDeclaration? = null

    override var bridgedSirConstructor: SirConstructor?
        get() {
            // Check this is constructor
            kotlinSirConstructor

            return bridgedSirCallableDeclaration as? SirConstructor
        }
        set(value) {
            // Check this is constructor
            kotlinSirConstructor

            bridgedSirCallableDeclaration = value
        }

    override var bridgedSirFunction: SirFunction?
        get() {
            // Check this is function
            kotlinSirFunction

            return bridgedSirCallableDeclaration as? SirFunction
        }
        set(value) {
            // Check this is function
            kotlinSirFunction

            bridgedSirCallableDeclaration = value
        }

    override val asyncSwiftModelOrNull: MutableKotlinFunctionSwiftModel?
        get() = with(swiftModelScope) {
            descriptor.asyncSwiftModelOrNull
        }

    override val owner: KotlinTypeSwiftModel?
        get() = with(swiftModelScope) {
            descriptor.owner()
        }

    override val receiver: SirType by lazy {
        with(swiftModelScope) {
            descriptor.receiverType()
        }
    }

    override val objCSelector: String by core::objCSelector

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

    override val objCReturnType: ObjCType?
        get() = with(skieContext) {
            core.getObjCReturnType(descriptor, descriptor.flowMappingStrategy)
        }

    override val hasValidSignatureInSwift: Boolean
        get() = when (role) {
            KotlinFunctionSwiftModel.Role.Constructor -> {
                kotlinSirValueParameters.map { it.type }.flatMap { it.allReferencedTypes() }
                    .none { it is SkieErrorSirType }
            }
            else -> {
                (listOf(kotlinSirFunction.returnType, receiver) + kotlinSirValueParameters.map { it.type }).flatMap { it.allReferencedTypes() }
                    .none { it is SkieErrorSirType }
            }
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
