package co.touchlab.skie.plugin.api.function

import co.touchlab.skie.plugin.api.type.MutableSwiftTypeName

interface MutableSwiftFunctionName : SwiftFunctionName {

    override var name: String
    override val parameterNames: List<MutableParameterName>

    override val receiverName: MutableSwiftTypeName

    fun rename(name: String, newParameterNames: List<String>)

    val isChanged: Boolean

    interface MutableParameterName : SwiftFunctionName.ParameterName {

        override var name: String

        val isChanged: Boolean
    }
}
