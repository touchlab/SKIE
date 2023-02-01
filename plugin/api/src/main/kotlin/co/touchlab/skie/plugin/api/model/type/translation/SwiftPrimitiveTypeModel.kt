package co.touchlab.skie.plugin.api.model.type.translation

sealed class SwiftPrimitiveTypeModel(
    val name: String,
) : SwiftTypeModel {

    object NSUInteger : SwiftPrimitiveTypeModel("NSUInteger")
    object Bool : SwiftPrimitiveTypeModel("Bool")
    @Suppress("ClassName")
    object unichar : SwiftPrimitiveTypeModel("unichar")
    object Int8 : SwiftPrimitiveTypeModel("Int8")
    object Int16 : SwiftPrimitiveTypeModel("Int16")
    object Int32 : SwiftPrimitiveTypeModel("Int32")
    object Int64 : SwiftPrimitiveTypeModel("Int64")
    object UInt8 : SwiftPrimitiveTypeModel("UInt8")
    object UInt16 : SwiftPrimitiveTypeModel("UInt16")
    object UInt32 : SwiftPrimitiveTypeModel("UInt32")
    object UInt64 : SwiftPrimitiveTypeModel("UInt64")
    object Float : SwiftPrimitiveTypeModel("Float")
    object Double : SwiftPrimitiveTypeModel("Double")

    override val stableFqName: String
        get() = name
}
