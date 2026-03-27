package `tests`.`bugs`.`type_and_parameter_name_collision_in_api_notes`.`interfaces`

interface I {

    fun foo(id: String, map: Map<String, Any>?) {
    }
}

@Suppress("LocalVariableName")
fun foo(KotlinI: I) {
}

@Suppress("LocalVariableName")
fun foo(KotlinI: I?) {
}

fun bar(id: Int): I = object : I {}

fun I.foo(id: String): I {
    return this
}
