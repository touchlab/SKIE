# SwiftCompilationError(error: cannot find 'A' in scope)

let result = await Task.detached {
    try! await A().foo()
}.value

exit(result.int32Value)
