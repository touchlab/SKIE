# SwiftCompilationError(error: value of type 'A' has no member 'foo')

let result = await Task.detached {
    try! await A().foo()
}.value

exit(result.int32Value)
