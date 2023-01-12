package co.touchlab.skie.plugin.api.model.property.extension

import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel

interface MutableKotlinInterfaceExtensionPropertySwiftModel : KotlinInterfaceExtensionPropertySwiftModel {

    override val getter: MutableKotlinFunctionSwiftModel

    override val setter: MutableKotlinFunctionSwiftModel?
}
