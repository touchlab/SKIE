let result = await Task.detached {
    try! await A().foo()
}.value

exit(result.int32Value)
