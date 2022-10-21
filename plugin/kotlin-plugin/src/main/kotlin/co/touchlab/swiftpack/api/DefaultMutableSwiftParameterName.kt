package co.touchlab.swiftpack.api

class DefaultMutableSwiftParameterName(
    override val originalName: String
): MutableSwiftFunctionName.MutableParameterName {
    override var name: String = originalName
    override val isChanged: Boolean
        get() = name != originalName
}
