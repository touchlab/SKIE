package co.touchlab.swiftpack.plugin

interface SwiftNameProvider {
    fun getSwiftName(kotlinClassName: String): String
}