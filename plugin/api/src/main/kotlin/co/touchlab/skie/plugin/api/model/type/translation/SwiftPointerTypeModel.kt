package co.touchlab.skie.plugin.api.model.type.translation

data class SwiftPointerTypeModel(
    val pointee: SwiftTypeModel,
    val nullable: Boolean = false,
): SwiftTypeModel {
    override val stableFqName: String
        get() = "UnsafeMutableRawPointer" + if (nullable) "?" else ""
}
