package `tests`.`bugs`.`lambda_type_argument`.`enum_function`

class A<T>

enum class E {
    X;

    fun foo(param: A<() -> Unit>) {
    }
}
