let result = await Task.detached {
    try! await AKt.foo(i: KotlinIntArray(size: 3, init: { KotlinInt(integerLiteral: $0.intValue - 1) }))
}.value

exit(result.int32Value)
