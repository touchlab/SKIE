let result = await Task.detached {
    try! await A1().foo_()
}.value

exit(result.int32Value - 1)
