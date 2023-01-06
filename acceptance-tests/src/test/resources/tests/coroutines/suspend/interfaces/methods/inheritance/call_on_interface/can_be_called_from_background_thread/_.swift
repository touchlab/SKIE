let result = await Task.detached {
    try! await (A1() as A).foo()
}.value

exit(result.int32Value)
