# RuntimeError(Fatal error: 'try!' expression unexpectedly raised an error: Error Domain=KotlinException Code=0 "Declared exception" UserInfo={NSLocalizedDescription=Declared exception, KotlinException=kotlin.IllegalStateException: Declared exception, KotlinExceptionOrigin=})

let result = await Task.detached {
    try! await AKt.foo()
}.value

exit(result.int32Value)
