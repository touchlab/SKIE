let result = await Task.detached {
    try! await AKt.foo { 0 }
}.value

exit(result.int32Value)
