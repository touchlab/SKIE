package co.touchlab.skie.plugin.api.function

interface SwiftFunctionName {
    val originalName: String
    val name: String

    val parameterNames: List<ParameterName>

    val originalQualifiedName: String
    val qualifiedName: String

    val originalReference: String
    val reference: String

    interface ParameterName {
        val originalName: String
        val name: String
    }
}
