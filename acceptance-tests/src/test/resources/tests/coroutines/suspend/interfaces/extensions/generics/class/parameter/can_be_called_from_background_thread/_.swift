let result = await Task.detached {
    try! await AKt.foo(A1<KotlinInt, KotlinInt>(), i: KotlinInt(1), k: 0)
}.value

exit(result.int32Value)
