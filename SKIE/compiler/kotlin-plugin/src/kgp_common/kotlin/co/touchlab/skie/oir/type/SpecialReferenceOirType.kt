package co.touchlab.skie.oir.type

sealed class SpecialReferenceOirType(private val name: String) : NonNullReferenceOirType() {

    object Id : SpecialReferenceOirType("id")

    object InstanceType : SpecialReferenceOirType("instancetype")

    object Class : SpecialReferenceOirType("Class")

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String =
        name.withAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
}
