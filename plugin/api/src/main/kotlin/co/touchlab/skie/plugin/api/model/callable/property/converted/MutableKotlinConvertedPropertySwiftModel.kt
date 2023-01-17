package co.touchlab.skie.plugin.api.model.callable.property.converted

import co.touchlab.skie.plugin.api.model.callable.function.MutableKotlinFunctionSwiftModel

interface MutableKotlinConvertedPropertySwiftModel : KotlinConvertedPropertySwiftModel {

    override val getter: MutableKotlinFunctionSwiftModel

    override val setter: MutableKotlinFunctionSwiftModel?
}
