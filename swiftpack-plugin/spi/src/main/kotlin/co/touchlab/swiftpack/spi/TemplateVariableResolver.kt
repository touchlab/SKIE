package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.SwiftTemplateVariable

interface TemplateVariableResolver {
    fun resolve(variableName: SwiftTemplateVariable.Name): String
}
