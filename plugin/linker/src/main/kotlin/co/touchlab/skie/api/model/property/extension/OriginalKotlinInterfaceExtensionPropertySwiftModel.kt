package co.touchlab.skie.api.model.property.extension

import co.touchlab.skie.plugin.api.model.function.KotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.function.MutableKotlinFunctionSwiftModel
import co.touchlab.skie.plugin.api.model.property.extension.KotlinInterfaceExtensionPropertySwiftModel
import org.jetbrains.kotlin.descriptors.PropertyDescriptor

class OriginalKotlinInterfaceExtensionPropertySwiftModel(
    override val descriptor: PropertyDescriptor,
    getter: MutableKotlinFunctionSwiftModel,
    setter: MutableKotlinFunctionSwiftModel?,
) : KotlinInterfaceExtensionPropertySwiftModel {

    override val original: KotlinInterfaceExtensionPropertySwiftModel = this

    override val getter: KotlinFunctionSwiftModel = getter.original

    override val setter: KotlinFunctionSwiftModel? = setter?.original
}
