package `tests`.`default_arguments`.`classes`.`methods`.`unit_return_type`

class A {

    var result = 1

    fun changeResult(i: Int = 0) {
        result = i
    }
}
