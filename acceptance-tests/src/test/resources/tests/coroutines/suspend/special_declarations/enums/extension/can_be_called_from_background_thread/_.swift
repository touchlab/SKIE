let result = await Task.detached {
    try! await A.a1.foo()
}.value

exit(result.int32Value)
