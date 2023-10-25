package co.touchlab.skie.oir.type

@Suppress("ClassName")
sealed class PrimitiveOirType(
    val name: String,
) : OirType() {

    object NSUInteger : PrimitiveOirType("NSUInteger")
    object BOOL : PrimitiveOirType("BOOL")
    object unichar : PrimitiveOirType("unichar")
    object int8_t : PrimitiveOirType("int8_t")
    object int16_t : PrimitiveOirType("int16_t")
    object int32_t : PrimitiveOirType("int32_t")
    object int64_t : PrimitiveOirType("int64_t")
    object uint8_t : PrimitiveOirType("uint8_t")
    object uint16_t : PrimitiveOirType("uint16_t")
    object uint32_t : PrimitiveOirType("uint32_t")
    object uint64_t : PrimitiveOirType("uint64_t")
    object float : PrimitiveOirType("float")
    object double : PrimitiveOirType("double")

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String =
        name.withAttrsAndName(attrsAndName)
}
