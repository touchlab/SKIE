let result = await Task.detached {
    try! await A().foo(i: 0)
}.value

exit(result.int32Value)
