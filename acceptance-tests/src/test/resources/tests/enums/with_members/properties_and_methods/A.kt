package `tests`.`enums`.`with_members`.`properties_and_methods`

enum class A {
    A1,
    A2;

    fun int8Return(): Byte = 1

    fun int16Return(): Short = 1

    fun int32Return(): Int = 1

    fun int64Return(): Long = 1

    fun floatReturn(): Float = 1.0f

    fun doubleReturn(): Double = 1.0

    fun stringReturn(): String = "1"

    fun booleanReturn(): Boolean = true

    fun voidReturn() {}

    fun int8ReturnWithParam(a: Byte): Byte = a

    fun int16ReturnWithParam(a: Short): Short = a

    fun int32ReturnWithParam(a: Int): Int = a

    fun int64ReturnWithParam(a: Long): Long = a

    fun floatReturnWithParam(a: Float): Float = a

    fun doubleReturnWithParam(a: Double): Double = a

    fun stringReturnWithParam(a: String): String = a

    fun booleanReturnWithParam(a: Boolean): Boolean = a

    @Throws(Exception::class)
    fun throwingFoo() {
    }

    suspend fun suspendingFoo() {}
}

fun a1(): A = A.A1

fun a2(): A = A.A2
