package `tests`.`functions`.`deprecation`.`warning`.`enum`

interface I {

    @Deprecated("Some warning", level = DeprecationLevel.WARNING)
    fun foo() {
    }
}

enum class A : I {
    a1;
}
