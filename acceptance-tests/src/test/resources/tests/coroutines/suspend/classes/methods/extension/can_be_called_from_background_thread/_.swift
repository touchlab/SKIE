let result = await Task.detached {
    try! await A(i: 1).foo(B(k: 1))
}.value

exit(result.int32Value)
