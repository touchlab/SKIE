package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_FUNCTION_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_PROPERTY_PREFIX
import co.touchlab.swiftpack.spec.SWIFTPACK_KOTLIN_TYPE_PREFIX
import co.touchlab.swiftpack.spec.SwiftPackModule

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
}
