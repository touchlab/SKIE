package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.api.model.callable.identifierAfterVisibilityChanges
import co.touchlab.skie.api.model.callable.swiftModelOrigin
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertyGetterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySetterSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCType
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinRegularPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    override val allBoundedSwiftModels: List<MutableKotlinDirectlyCallableMemberSwiftModel>,
    private val core: KotlinRegularPropertySwiftModelCore,
    private val swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinRegularPropertySwiftModel {

    override val receiver: TypeSwiftModel by lazy {
        with(swiftModelScope) {
            descriptor.receiverTypeModel()
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

    override val original: KotlinRegularPropertySwiftModel = OriginalKotlinRegularPropertySwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility

    override val origin: KotlinCallableMemberSwiftModel.Origin = descriptor.swiftModelOrigin

    override val type: TypeSwiftModel
        get() = with(swiftModelScope) {
            core.descriptor.propertyTypeModel(receiver.swiftGenericExportScope)
        }

    override val objCType: ObjCType by lazy {
        core.getObjCType(descriptor)
    }

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
