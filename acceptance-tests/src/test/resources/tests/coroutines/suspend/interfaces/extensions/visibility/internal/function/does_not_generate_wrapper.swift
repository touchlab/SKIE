# SwiftCompilationError(error: type 'AKt' has no member 'foo')

let result = await Task.detached {
    try! await AKt.foo(A1())
}.value

exit(result.int32Value)
