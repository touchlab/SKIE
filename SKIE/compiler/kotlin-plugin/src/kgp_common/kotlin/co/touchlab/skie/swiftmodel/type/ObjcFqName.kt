package co.touchlab.skie.swiftmodel.type

@JvmInline
value class ObjcFqName(
    val name: String,
) {

    fun asString(): String = name

    override fun toString(): String = asString()
}
