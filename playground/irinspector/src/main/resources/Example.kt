suspend fun foo() {

}

suspend fun callFoo() {
    foo()
    foo()
    foo()
}
