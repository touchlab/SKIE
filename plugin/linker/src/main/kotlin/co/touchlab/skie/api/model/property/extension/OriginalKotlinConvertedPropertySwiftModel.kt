package co.touchlab.skie.api.model.property.extension

import co.touchlab.skie.plugin.api.model.callable.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class OriginalKotlinConvertedPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    getter: MutableKotlinFunctionSwiftModel,
    setter: MutableKotlinFunctionSwiftModel?,
) : KotlinConvertedPropertySwiftModel {

    override val original: KotlinConvertedPropertySwiftModel = this

    override val getter: KotlinFunctionSwiftModel = getter.original

    override val setter: KotlinFunctionSwiftModel? = setter?.original
}
