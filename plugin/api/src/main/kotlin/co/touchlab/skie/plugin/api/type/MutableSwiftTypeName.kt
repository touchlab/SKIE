package co.touchlab.skie.plugin.api.type

interface MutableSwiftTypeName : SwiftTypeName {

    override var parent: MutableSwiftTypeName?
    override var simpleName: String

    val isChanged: Boolean
    val isSimpleNameChanged: Boolean
}
