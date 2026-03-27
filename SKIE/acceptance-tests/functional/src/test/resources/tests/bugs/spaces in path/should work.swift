let result = await Task.detached {
    try! await AA1().foo()
}.value

exit(result.int32Value)
