let result = await Task.detached {
    try! await AKt.foo(A<KotlinInt>(), i: 0)
}.value

exit(result.int32Value)
