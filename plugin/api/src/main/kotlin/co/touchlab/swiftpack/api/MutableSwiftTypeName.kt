package co.touchlab.swiftpack.api

interface MutableSwiftTypeName: SwiftTypeName {
    override var parent: MutableSwiftTypeName?
    override var separator: String
    override var simpleName: String

    val isChanged: Boolean
}
