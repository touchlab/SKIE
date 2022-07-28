package co.touchlab.swiftpack.spi

interface SwiftNameProvider {
    fun getSwiftName(kotlinClassName: String): String
}