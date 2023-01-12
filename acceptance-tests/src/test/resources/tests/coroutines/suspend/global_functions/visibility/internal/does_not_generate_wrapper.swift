# SwiftCompilationError(error: cannot find 'AKt' in scope)

let result = await Task.detached {
    try! await AKt.foo()
}.value

exit(result.int32Value)
