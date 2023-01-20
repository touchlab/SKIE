package co.touchlab.skie.api.model.callable.property.converted

import co.touchlab.skie.plugin.api.model.callable.property.converted.KotlinConvertedPropertySwiftModel

class OriginalKotlinConvertedPropertySwiftModel(
    delegate: KotlinConvertedPropertySwiftModel,
) : KotlinConvertedPropertySwiftModel by delegate
