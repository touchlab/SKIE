let result = await Task.detached {
    try! await A().foo(B(k: 1))
}.value

exit(result.int32Value)
