await Task.detached {
    try! await AKt.foo()
}.value

exit(AKt.result)
