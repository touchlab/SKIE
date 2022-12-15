package co.touchlab.skie.plugin.api.function

import co.touchlab.skie.plugin.api.type.SwiftTypeName

interface SwiftFunctionName {

    val originalName: String
    val name: String

    val parameterNames: List<ParameterName>

    val originalQualifiedName: String
    val qualifiedName: String

    val originalReference: String
    val reference: String

    val receiverName: SwiftTypeName

    interface ParameterName {

        val originalName: String
        val name: String
    }
}
