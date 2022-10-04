package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_ENUM_ENTRY_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_FUNCTION_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_PROPERTY_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_TYPE_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_TEMPLATE_VARIABLE_PREFIX
import co.touchlab.swiftpack.spec.SwiftPackModule
import co.touchlab.swiftpack.spec.SwiftPackModule2
import co.touchlab.swiftpack.spec.SwiftTemplateVariable

fun SwiftPackModule.TemplateFile.produceSwiftFile(swiftNameProvider: SwiftNameProvider): String {
    return contents
        .replace("($SWIFTPACK_KOTLIN_TYPE_PREFIX[a-zA-Z0-9_]+)".toRegex()) { match ->
            swiftNameProvider.getSwiftTypeName(match.groupValues[1])
        }
        .replace("($SWIFTPACK_KOTLIN_PROPERTY_PREFIX[a-zA-Z0-9_]+)".toRegex()) { match ->
            swiftNameProvider.getSwiftPropertyName(match.groupValues[1])
        }
        .replace("($SWIFTPACK_KOTLIN_FUNCTION_PREFIX[a-zA-Z0-9_]+)".toRegex()) { match ->
            swiftNameProvider.getSwiftFunctionSelector(match.groupValues[1])
        }
        .replace("($SWIFTPACK_KOTLIN_ENUM_ENTRY_PREFIX[a-zA-Z0-9_]+)".toRegex()) { match ->
            swiftNameProvider.getSwiftEnumEntryName(match.groupValues[1])
        }
}

fun SwiftPackModule2.TemplateFile.produceSwiftFile(templateVariableResolver: TemplateVariableResolver): String {
    return contents.replace("($SWIFTPACK_TEMPLATE_VARIABLE_PREFIX[a-zA-Z0-9_]+)".toRegex()) { match ->
        templateVariableResolver.resolve(SwiftTemplateVariable.Name(match.groupValues[1]))
    }
}
