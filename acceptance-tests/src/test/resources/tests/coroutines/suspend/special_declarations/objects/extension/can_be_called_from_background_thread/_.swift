let result = await Task.detached {
    try! await A.shared.foo()
}.value

exit(result.int32Value)
