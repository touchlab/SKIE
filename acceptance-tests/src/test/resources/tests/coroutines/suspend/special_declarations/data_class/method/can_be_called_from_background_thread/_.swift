let result = await Task.detached {
    try! await A(i: 0).foo()
}.value

exit(result.int32Value)
