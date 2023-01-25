package co.touchlab.skie.plugin.api.model.type.translation

import io.outfoxx.swiftpoet.DeclaredTypeName

data class SwiftRawTypeModel(
    val type: DeclaredTypeName,
): SwiftTypeModel {
    override val stableFqName: String
        get() = TODO("Not yet implemented")
}
