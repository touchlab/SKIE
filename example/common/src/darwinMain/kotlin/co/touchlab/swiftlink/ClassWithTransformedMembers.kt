package co.touchlab.swiftlink

class ClassWithTransformedMembers {

    @Test(hide = true)
    val toBeHiddenProperty: String = "hidden"

    @Test(rename = "renamedProperty")
    val toBeRenamedProperty: String = "renamed"

    @Test(hide = true)
    fun toBeHiddenMethod(): String = "hidden"

    @Test(rename = "renamedMethod()")
    fun toBeRenamedMethod(): String = "renamed"

    @Test(hide = true)
    fun toBeHiddenMethodWithParams(param: String): String = "hidden"

    @Test(invocation = "()")
    fun overloadedMethod(): String = "overloaded"

    @Test(invocation = "(\"Hello\")")
    fun overloadedMethod(param: String): String = "overloaded with param = $param"
}
