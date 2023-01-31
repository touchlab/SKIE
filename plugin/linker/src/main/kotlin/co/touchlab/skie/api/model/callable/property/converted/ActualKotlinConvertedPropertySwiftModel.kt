package co.touchlab.skie.api.model.callable.property.converted

import co.touchlab.skie.api.model.callable.swiftModelKind
import co.touchlab.skie.plugin.api.model.MutableSwiftModelScope
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.KotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModel
import co.touchlab.skie.plugin.api.model.callable.MutableKotlinCallableMemberSwiftModelVisitor
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.type.TypeSwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinConvertedPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    override val allBoundedSwiftModels: List<MutableKotlinCallableMemberSwiftModel>,
    swiftModelScope: MutableSwiftModelScope,
) : MutableKotlinConvertedPropertySwiftModel {

    override val receiver: TypeSwiftModel by lazy {
        with(swiftModelScope) {
            descriptor.receiverTypeModel()
        }
    }

    override val getter: MutableKotlinFunctionSwiftModel by lazy {
        with(swiftModelScope) {
            descriptor.getter?.swiftModel ?: error("Property does not have a getter: $descriptor")
        }
    }

    override val setter: MutableKotlinFunctionSwiftModel? by lazy {
        with(swiftModelScope) {
            descriptor.setter?.swiftModel
        }
    }

    override val original: KotlinConvertedPropertySwiftModel = OriginalKotlinConvertedPropertySwiftModel(this)

    override val kind: KotlinCallableMemberSwiftModel.Kind = descriptor.swiftModelKind

    override fun toString(): String = descriptor.toString()

    override fun <OUT> accept(visitor: KotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)

    override fun <OUT> accept(visitor: MutableKotlinCallableMemberSwiftModelVisitor<OUT>): OUT =
        visitor.visit(this)
}
