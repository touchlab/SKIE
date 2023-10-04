package co.touchlab.skie.swiftmodel.callable.property.regular

import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.sir.element.SirCallableDeclaration
import co.touchlab.skie.sir.element.SirProperty
import co.touchlab.skie.sir.flowMappingStrategy
import co.touchlab.skie.sir.type.SirType
import co.touchlab.skie.sir.type.SkieErrorSirType
import co.touchlab.skie.swiftmodel.MutableSwiftModelScope
import co.touchlab.skie.swiftmodel.SwiftModelVisibility
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.swiftmodel.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.swiftmodel.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.swiftmodel.callable.swiftModelOrigin
import co.touchlab.skie.swiftmodel.type.KotlinTypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinRegularPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    kotlinSirPropertyFactory: () -> SirProperty,
    override val allBoundedSwiftModels: List<MutableKotlinDirectlyCallableMemberSwiftModel>,
    private val core: KotlinRegularPropertySwiftModelCore,
    private val swiftModelScope: MutableSwiftModelScope,
    descriptorProvider: DescriptorProvider,
    private val skieContext: SkiePhase.Context,
) : MutableKotlinRegularPropertySwiftModel {

    override val kotlinSirProperty: SirProperty by lazy {
        kotlinSirPropertyFactory()
    }

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

    override val kotlinSirCallableDeclaration: SirCallableDeclaration by ::kotlinSirProperty

    override var bridgedSirProperty: SirProperty? = null

    override val bridgedSirCallableDeclaration: SirCallableDeclaration? by ::bridgedSirProperty

    override val objCName: String by core::objCName

    override var collisionResolutionStrategy: CollisionResolutionStrategy = CollisionResolutionStrategy.Rename

    override val origin: KotlinCallableMemberSwiftModel.Origin = descriptor.swiftModelOrigin

    override val scope: KotlinCallableMemberSwiftModel.Scope =
        if (descriptorProvider.getReceiverClassDescriptorOrNull(descriptor) == null) {
            KotlinCallableMemberSwiftModel.Scope.Static
        } else {
            KotlinCallableMemberSwiftModel.Scope.Member
        }

    override val objCType: ObjCType
        get() = with(skieContext) {
            core.getObjCType(descriptor, descriptor.flowMappingStrategy)
        }

    override val hasValidSignatureInSwift: Boolean
        get() = listOf(kotlinSirProperty.type, receiver).flatMap { it.allReferencedTypes() }
            .none { it is SkieErrorSirType }

    override val getter: KotlinRegularPropertyGetterSwiftModel = DefaultKotlinRegularPropertyGetterSwiftModel(
        descriptor.getter ?: error("$descriptor does not have a getter."),
        core.namer,
    )

    override val setter: KotlinRegularPropertySetterSwiftModel? = descriptor.setter?.let {
        DefaultKotlinRegularPropertySetterSwiftModel(it, core.namer)
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
