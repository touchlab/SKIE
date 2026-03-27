# SwiftCompilationError(error: 'foo' is inaccessible due to 'internal' protection level)

let result = await Task.detached {
    try! await A().foo()
}.value

exit(result.int32Value)
