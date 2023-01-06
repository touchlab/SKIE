let result = await Task.detached {
    try! await AKt.foo(A1(), i: 0, k: 1)
}.value

exit(result.int32Value)
