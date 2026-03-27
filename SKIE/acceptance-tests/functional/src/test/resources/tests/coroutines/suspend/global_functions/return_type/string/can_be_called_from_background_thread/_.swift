let result = await Task.detached {
    try! await AKt.foo()
}.value

exit(Int32(result.count - 1))
