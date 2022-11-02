package co.touchlab.skie.plugin.api

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
