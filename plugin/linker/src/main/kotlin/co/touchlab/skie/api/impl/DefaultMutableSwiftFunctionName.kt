package co.touchlab.skie.api.impl

import co.touchlab.skie.plugin.api.function.MutableSwiftFunctionName

class DefaultMutableSwiftFunctionName(
    override val originalName: String,
    override val parameterNames: List<MutableSwiftFunctionName.MutableParameterName>,
) : MutableSwiftFunctionName {
    override var name: String = originalName

    override val originalQualifiedName: String
        get() = "$originalName(${parameterNames.joinToString("") { "${it.originalName}:" }})"
    override val qualifiedName: String
        get() = "$name(${parameterNames.joinToString("") { "${it.name}:" }})"

    override val originalReference: String
        get() = if (parameterNames.isEmpty()) {
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
