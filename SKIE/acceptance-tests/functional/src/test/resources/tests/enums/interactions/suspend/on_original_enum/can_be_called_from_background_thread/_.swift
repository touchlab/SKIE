let result = await Task.detached {
    try! await (A.a1 as __A).foo()
}.value

exit(result.int32Value)
