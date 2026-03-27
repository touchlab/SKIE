package `tests`.`enums`.`nested`.`companion_in_enum`

enum class A {
    A1,
    A2;

    companion object {

    }
}

interface EnumInterface {

    fun foo()
}

interface EnumCompanionInterface {

    fun bar()
}

enum class B : EnumInterface {
    B1,
    B2;

    override fun foo() {
        throw UnsupportedOperationException()
    }

    companion object : EnumCompanionInterface {

        override fun bar() {
            throw UnsupportedOperationException()
        }
    }
}

fun a1(): A = A.A1
fun a2(): A = A.A2

fun companion(): A.Companion = A.Companion
