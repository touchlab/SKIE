package co.touchlab.skie.api.model.callable.property.regular

import co.touchlab.skie.api.model.callable.swiftModelKind
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.SwiftModelVisibility
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinDirectlyCallableMemberSwiftModel.CollisionResolutionStrategy
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinDirectlyCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.KotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.regular.MutableKotlinRegularPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
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

    override var collisionResolutionStrategy: CollisionResolutionStrategy = CollisionResolutionStrategy.Rename

    override val original: KotlinRegularPropertySwiftModel = OriginalKotlinRegularPropertySwiftModel(this)

    override val isChanged: Boolean
        get() = identifier != original.identifier || visibility != original.visibility

    override val kind: KotlinCallableMemberSwiftModel.Kind = descriptor.swiftModelKind

    override val type: TypeSwiftModel
        get() = with(swiftModelScope) {
            core.descriptor.propertyTypeModel(receiver.swiftGenericExportScope)
        }

    override fun toString(): String = descriptor.toString()
}
