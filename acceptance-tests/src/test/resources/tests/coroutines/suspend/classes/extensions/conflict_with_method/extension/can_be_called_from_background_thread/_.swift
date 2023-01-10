let result = await Task.detached {
    try! await A().foo_()
}.value

exit(result.int32Value - 1)
