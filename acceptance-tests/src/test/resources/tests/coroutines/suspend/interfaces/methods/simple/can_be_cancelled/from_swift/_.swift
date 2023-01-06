let task = Task {
    try await (A1() as A).foo().int32Value
}

task.cancel()

do {
   exit(try await task.value)
} catch {
    exit(-1)
}
