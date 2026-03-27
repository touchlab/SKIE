let result = await Task.detached {
    try! await AKt.get(i: 0)
}.value

exit(result.int32Value)
