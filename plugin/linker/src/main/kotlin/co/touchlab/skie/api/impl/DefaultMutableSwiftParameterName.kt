package co.touchlab.skie.api.impl

import co.touchlab.skie.plugin.api.function.MutableSwiftFunctionName

class DefaultMutableSwiftParameterName(
    override val originalName: String,
) : MutableSwiftFunctionName.MutableParameterName {

    override var name: String = originalName
    override val isChanged: Boolean
        get() = name != originalName
}
