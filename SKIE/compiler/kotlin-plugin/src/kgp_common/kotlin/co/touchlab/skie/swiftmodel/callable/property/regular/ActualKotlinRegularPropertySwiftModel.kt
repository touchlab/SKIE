package co.touchlab.skie.swiftmodel.callable.property.regular

import co.touchlab.skie.swiftmodel.callable.identifierAfterVisibilityChanges
import co.touchlab.skie.swiftmodel.callable.swiftModelOrigin
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.swiftGenericExportScope
import co.touchlab.skie.swiftmodel.type.FlowMappingStrategy
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SkieErrorSirType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinRegularPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    override val allBoundedSwiftModels: List<MutableKotlinDirectlyCallableMemberSwiftModel>,
    private val core: KotlinRegularPropertySwiftModelCore,
    private val swiftModelScope: MutableSwiftModelScope,
    descriptorProvider: DescriptorProvider,
) : MutableKotlinRegularPropertySwiftModel {

    override val owner: KotlinTypeSwiftModel? by lazy {
        with(swiftModelScope) {
            descriptor.owner()
        }
    }

    override val receiver: SirType by lazy {
        with(swiftModelScope) {
            descriptor.receiverType()
        }
    }

    override var identifier: String by core::identifier

    override var visibility: SwiftModelVisibility by core::visibility

    override val objCName: String by core::objCName

    override val reference: String
        get() = identifierAfterVisibilityChanges

    override val name: String
        get() = reference

    override var collisionResolutionStrategy: CollisionResolutionStrategy = CollisionResolutionStrategy.Rename

    override val origin: KotlinCallableMemberSwiftModel.Origin = descriptor.swiftModelOrigin

    override val scope: KotlinCallableMemberSwiftModel.Scope =
        if (descriptorProvider.getReceiverClassDescriptorOrNull(descriptor) == null) {
            KotlinCallableMemberSwiftModel.Scope.Static
        } else {
            KotlinCallableMemberSwiftModel.Scope.Member
        }

    override val type: SirType
        get() = with(swiftModelScope) {
            descriptor.propertyType(core.descriptor, swiftGenericExportScope, flowMappingStrategy)
        }

    override var flowMappingStrategy: FlowMappingStrategy = FlowMappingStrategy.None

    override val objCType: ObjCType
        get() = core.getObjCType(descriptor, flowMappingStrategy)

    override val hasValidSignatureInSwift: Boolean
        get() = listOf(type, receiver).flatMap { it.allReferencedTypes() }
            .none { it is SkieErrorSirType }

    override val getter: KotlinRegularPropertyGetterSwiftModel by core::getter

    override val setter: KotlinRegularPropertySetterSwiftModel? by core::setter

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
