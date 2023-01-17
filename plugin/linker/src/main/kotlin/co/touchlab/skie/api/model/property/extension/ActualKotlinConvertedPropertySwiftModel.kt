package co.touchlab.skie.api.model.property.extension

import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.MutableKotlinConvertedPropertySwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class ActualKotlinConvertedPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    override val getter: MutableKotlinFunctionSwiftModel,
    override val setter: MutableKotlinFunctionSwiftModel?,
) : MutableKotlinConvertedPropertySwiftModel {

    override val original: KotlinConvertedPropertySwiftModel = OriginalKotlinConvertedPropertySwiftModel(
        descriptor,
        getter,
        setter,
    )
}
