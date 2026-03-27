let result = await Task.detached {
    try! await A1().foo()
}.value

exit(result.int32Value)
