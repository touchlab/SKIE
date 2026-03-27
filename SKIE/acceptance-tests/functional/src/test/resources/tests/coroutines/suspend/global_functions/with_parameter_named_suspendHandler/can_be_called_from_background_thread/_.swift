let result = await Task.detached {
    try! await AKt.foo(suspendHandler: 0, _suspendHandler: 1)
}.value

exit(result.int32Value)
