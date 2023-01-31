package co.touchlab.skie.plugin.api.model.type.translation

data class SwiftProtocolTypeModel(
    val protocolName: String,
) : SwiftNonNullReferenceTypeModel {

    override val stableFqName: String
        get() = protocolName
}
