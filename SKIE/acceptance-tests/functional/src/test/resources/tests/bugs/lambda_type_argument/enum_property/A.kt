package `tests`.`bugs`.`lambda_type_argument`.`enum_property`

class A<T>

enum class E {
    X;

    var foo: A<() -> Unit>
        get() = error("")
        set(value) {}
}
