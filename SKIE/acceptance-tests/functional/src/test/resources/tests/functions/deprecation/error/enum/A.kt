package `tests`.`functions`.`deprecation`.`error`.`enum`

interface I {

    @Deprecated("Some error", level = DeprecationLevel.ERROR)
    fun foo() {
    }
}

enum class A : I {
    a1;
}
