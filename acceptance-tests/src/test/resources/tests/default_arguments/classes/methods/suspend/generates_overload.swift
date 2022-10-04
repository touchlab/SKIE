func callFoo(a: A) async {
    try! await a.foo()
}

exit(0)