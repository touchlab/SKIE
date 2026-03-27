let task = Task {
    try await AKt.foo().int32Value
}

task.cancel()

do {
   exit(try await task.value)
} catch {
    exit(-1)
}
