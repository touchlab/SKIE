package co.touchlab.skie.oir.type

sealed class SpecialReferenceOirType(val name: String) : NonNullReferenceOirType() {

    object Id : SpecialReferenceOirType("id")

    object InstanceType : SpecialReferenceOirType("instancetype")

    object Class : SpecialReferenceOirType("Class")

    object Protocol : SpecialReferenceOirType("Protocol") {

        override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String = buildString {
            append(name)
            append(" *")
            appendAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
        }
    }

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String =
        name.withAttrsAndName(attrsAndName.plusNonnullAttributeIfNeeded(needsNonnullAttribute))
}
