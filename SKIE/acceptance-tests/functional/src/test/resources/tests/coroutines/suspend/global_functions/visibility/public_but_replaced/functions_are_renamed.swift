let result = try! await Task.detached {
    try! await AKt.__foo().int32Value
}.value + AKt.____foo().int32Value

exit(result)
