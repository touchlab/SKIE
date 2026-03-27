let result = await Task.detached {
    try! await A.Companion.shared.foo()
}.value

exit(result.int32Value)
