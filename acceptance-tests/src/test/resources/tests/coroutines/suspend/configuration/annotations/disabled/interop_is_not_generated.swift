# RuntimeError(Calling Kotlin suspend functions from Swift/Objective-C is currently supported only on main thread)

let result = await Task.detached {
    try! await A().foo()
}.value

exit(result.int32Value)
