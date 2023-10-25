package co.touchlab.skie.oir.type

object VoidOirType : OirType() {

    override fun render(attrsAndName: String, needsNonnullAttribute: Boolean): String =
        "void".withAttrsAndName(attrsAndName)
}
