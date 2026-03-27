package `tests`.`functions`.`deprecation`.`hidden`.`overriden`

interface I {

    fun foo(): Int = 0
}

class A : I {

    @Deprecated(message = "Some error", level = DeprecationLevel.HIDDEN)
    override fun foo(): Int = 1
}
