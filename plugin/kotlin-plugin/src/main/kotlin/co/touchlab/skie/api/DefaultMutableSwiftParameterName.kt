package co.touchlab.skie.api

import co.touchlab.skie.plugin.api.MutableSwiftFunctionName

class DefaultMutableSwiftParameterName(
    override val originalName: String,
) : MutableSwiftFunctionName.MutableParameterName {
    override var name: String = originalName
    override val isChanged: Boolean
        get() = name != originalName
}
