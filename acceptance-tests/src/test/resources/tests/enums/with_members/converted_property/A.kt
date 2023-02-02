package `tests`.`enums`.`with_members`.`converted_property`

var storage = 0

enum class A {

    X;

    @Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
    var <T: Int> T.variable: T
        get() = (storage - this as Int) as T
        set(value) {
            storage = this - value
        }
}
