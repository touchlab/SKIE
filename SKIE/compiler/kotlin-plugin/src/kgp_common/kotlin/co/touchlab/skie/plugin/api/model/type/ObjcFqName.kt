package co.touchlab.skie.plugin.api.model.type

@JvmInline
value class ObjcFqName(
    val name: String,
) {

    fun asString(): String = name

    override fun toString(): String = asString()
}
