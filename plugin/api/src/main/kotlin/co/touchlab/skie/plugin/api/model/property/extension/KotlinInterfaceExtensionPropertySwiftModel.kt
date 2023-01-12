package co.touchlab.skie.plugin.api.model.property.extension

import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.property.KotlinPropertySwiftModel

interface KotlinInterfaceExtensionPropertySwiftModel : KotlinPropertySwiftModel {

    val original: KotlinInterfaceExtensionPropertySwiftModel

    val getter: KotlinFunctionSwiftModel

    val setter: KotlinFunctionSwiftModel?

    val accessors: List<KotlinFunctionSwiftModel>
        get() = listOfNotNull(getter, setter)
}
