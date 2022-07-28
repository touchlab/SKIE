package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spi.SwiftNameProvider

class SimpleSwiftNameProvider: SwiftNameProvider {
    override fun getSwiftName(kotlinClassName: String): String {
        val packageSeparator = kotlinClassName.lastIndexOf('.')
        val (packageName, className) = if (packageSeparator == -1) {
            "" to kotlinClassName
        } else {
            kotlinClassName.substring(0, packageSeparator) to kotlinClassName.substring(packageSeparator + 1)
        }

        return when {
            packageName == "kotlin" -> "Kotlin$className"
            else -> className
        }
    }
}
