package `tests`.`bugs`.`type_and_parameter_name_collision_in_api_notes`.`classes`

fun foo(id: Any) {
}

fun foo2(id: Int): List<Any> = throw UnsupportedOperationException()

class A

@Suppress("LocalVariableName")
fun foo(KotlinA: A) {
}

@Suppress("LocalVariableName")
fun foo(KotlinA: A?) {
}

class B<T> {

    @Suppress("LocalVariableName")
    fun foo(KotlinB: B<T>) {
    }
}
