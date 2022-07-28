package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.NameMangling.demangledClassName
import co.touchlab.swiftpack.spec.SwiftPackModule

fun SwiftPackModule.TemplateFile.produceSwiftFile(swiftNameProvider: SwiftNameProvider): String {
    return contents.replace("KotlinSwiftGen\\.([a-zA-Z0-9_]+)".toRegex()) { match ->
        swiftNameProvider.getSwiftName(match.groupValues[1].demangledClassName)
    }
}