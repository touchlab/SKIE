# RuntimeError(Fatal error: 'try!' expression unexpectedly raised an error: Swift.CancellationError())

let result = await Task.detached {
    try! await AKt.foo()
}.value

exit(result.int32Value)
