package co.touchlab.skie.api

import co.touchlab.skie.plugin.api.MutableSwiftFunctionName
import co.touchlab.skie.plugin.api.SwiftFunctionName

class DefaultMutableSwiftFunctionName(
    override val originalName: String,
    override val originalParameterNames: List<SwiftFunctionName.ParameterName>,
    override var name: String,
    override val parameterNames: List<MutableSwiftFunctionName.MutableParameterName>,
) : MutableSwiftFunctionName {
    override val originalQualifiedName: String
        get() = "$originalName(${originalParameterNames.joinToString("") { "${it.originalName}:" }})"
    override val qualifiedName: String
        get() = "$name(${parameterNames.joinToString("") { "${it.name}:" }})"

    override val originalReference: String
        get() = if (originalParameterNames.isEmpty()) {
            originalName
        } else {
            originalQualifiedName
        }

    override val reference: String
        get() = if (parameterNames.isEmpty()) {
            name
        } else {
            qualifiedName
        }

    override val isChanged: Boolean
        get() = name != originalName || parameterNames.any { it.isChanged }

    override fun rename(name: String, newParameterNames: List<String>) {
        require(newParameterNames.size == parameterNames.size) {
            "New parameter names size (${newParameterNames.size}) must be equal to original parameter names size (${parameterNames.size})"
        }
        this.name = name
        parameterNames.forEachIndexed { index, parameterName ->
            parameterName.name = newParameterNames[index]
        }
    }
}
