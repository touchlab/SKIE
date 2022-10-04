package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.module.SwiftTemplateVariable

interface TemplateVariableResolver {
    fun resolve(variableName: SwiftTemplateVariable.Name): String
}
