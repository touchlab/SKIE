package co.touchlab.skie.oir.type

import org.jetbrains.kotlin.backend.konan.objcexport.objcNullableAttribute

data class PointerOirType(
    val pointee: OirType,
    val nullable: Boolean,
) : OirType() {

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String {
        val nullabilityAttribute = if (nullable) objcNullableAttribute else objcNonnullAttribute

        return pointee.render("*".withAttrsAndName(nullabilityAttribute).withAttrsAndName(attrsAndName), false)
    }
}
