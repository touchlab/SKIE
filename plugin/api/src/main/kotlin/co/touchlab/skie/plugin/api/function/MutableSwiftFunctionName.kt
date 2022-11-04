package co.touchlab.skie.plugin.api.function

interface MutableSwiftFunctionName : SwiftFunctionName {
    override var name: String
    override val parameterNames: List<MutableParameterName>

    fun rename(name: String, newParameterNames: List<String>)

    val isChanged: Boolean

    interface MutableParameterName : SwiftFunctionName.ParameterName {
        override var name: String

        val isChanged: Boolean
    }
}
