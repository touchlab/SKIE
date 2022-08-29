package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.SwiftPackReference

interface SwiftNameProvider {
    fun getSwiftTypeName(kotlinTypeReference: SwiftPackReference): String

    fun getSwiftPropertyName(kotlinPropertyReference: SwiftPackReference): String

    fun getSwiftFunctionSelector(kotlinFunctionReference: SwiftPackReference): String

    fun getSwiftEnumEntryName(kotlinEnumEntryReference: SwiftPackReference): String
}
