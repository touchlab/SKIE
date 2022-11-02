package `tests`.`enums`.`with_members`.`methods`

enum class A {
    A1 {
        override fun abstractFun(): Int = 1
    },
    A2 {
        override fun abstractFun(): Int = 2

        override fun overridableFun(): Int = 1
    };

    fun noParam() = Unit

    fun singleParam(p: Int) = Unit

    fun twoParams(p1: Int, p2: String) = Unit

    fun threeParams(p1: Int, p2: String, p3: Double) = Unit

    abstract fun abstractFun(): Int

    open fun overridableFun(): Int = 0

    @Throws(Exception::class)
    fun throwingNoParam() {
        throw Exception()
    }

    @Throws(Exception::class)
    fun throwingSingleParam(p: Int) {
        throw Exception()
    }

    @Throws(Exception::class)
    fun throwingTwoParams(p1: Int, p2: String) {
        throw Exception()
    }

    @Throws(Exception::class)
    fun throwingThreeParams(p1: Int, p2: String, p3: Double) {
        throw Exception()
    }

    suspend fun suspendingNoParam() {}

    suspend fun suspendingSingleParam(p: Int) {}

    suspend fun suspendingTwoParams(p1: Int, p2: String) {}

    suspend fun suspendingThreeParams(p1: Int, p2: String, p3: Double) {}

    @Throws(Exception::class)
    suspend fun suspendingThrowingNoParam() {
        throw Exception()
    }

    @Throws(Exception::class)
    suspend fun suspendingThrowingSingleParam(p: Int) {
        throw Exception()
    }

    @Throws(Exception::class)
    suspend fun suspendingThrowingTwoParams(p1: Int, p2: String) {
        throw Exception()
    }

    @Throws(Exception::class)
    suspend fun suspendingThrowingThreeParams(p1: Int, p2: String, p3: Double) {
        throw Exception()
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun inlineNoParam() = Unit

    @Suppress("NOTHING_TO_INLINE")
    inline fun inlineSingleParam(p: Int) = Unit

    inline fun inlineSingleParamClosure(p1: () -> Unit) = p1()

    inline fun inlineSingleParamCrossinlineClosure(crossinline p1: () -> Unit) = p1()

    @Suppress("NOTHING_TO_INLINE")
    inline fun inlineSingleParamNoinlineClosure(noinline p1: () -> Unit) = p1()

    fun singleVarargParam(vararg p: Int) = Unit

    fun singleParamSingleVarargParamSameType(p1: Int, vararg p: Int) = Unit

    fun singleParamSingleVarargParamDifferentType(p1: Int, vararg p: String) = Unit


    fun closureSingleParam(p1: (Int) -> Unit) = p1(0)

    fun closureTwoParams(p1: (Int, String) -> Unit) = p1(0, "")

    fun closureSingleParamReturnInt(p1: (Int) -> Int) = p1(0)

    fun closureTwoParamsReturnString(p1: (Int, String) -> String) = p1(0, "")

    companion object {
        fun staticFun() = Unit
    }
}

fun a1(): A = A.A1

fun a2(): A = A.A2
