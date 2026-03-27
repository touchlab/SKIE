package `tests`.`bugs`.`default_implementation_wrong_return_type`

interface GenericBound

class GenericImpl : GenericBound

interface I<G: GenericBound> {

    fun implementMe(): G

    fun default(): G? = try {
        implementMe()
    } catch (t: Throwable) {
        null
    }

}

enum class A: I<GenericImpl> {
    A1;

    override fun implementMe(): GenericImpl {
        return GenericImpl()
    }
}

class B: I<GenericImpl> {
    override fun implementMe(): GenericImpl {
        return GenericImpl()
    }
}

class C: I<GenericImpl> {
    override fun implementMe(): GenericImpl {
        return GenericImpl()
    }

    override fun default(): GenericImpl? {
        return super.default()
    }
}
