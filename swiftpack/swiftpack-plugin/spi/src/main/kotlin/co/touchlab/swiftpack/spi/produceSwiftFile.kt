package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.module.SWIFTPACK_TEMPLATE_VARIABLE_PREFIX
import co.touchlab.swiftpack.spec.module.SwiftPackModule
import co.touchlab.swiftpack.spec.module.SwiftTemplateVariable

fun SwiftPackModule.TemplateFile.produceSwiftFile(templateVariableResolver: TemplateVariableResolver): String {
    return contents.replace("($SWIFTPACK_TEMPLATE_VARIABLE_PREFIX[a-zA-Z0-9_]+)".toRegex()) { match ->
        templateVariableResolver.resolve(SwiftTemplateVariable.Name(match.groupValues[1]))
    }
}
