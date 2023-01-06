let task = Task {
    try await AKt.foo(A1())
}

do {
   try await task.value
} catch is CancellationError {
    exit(0)
} catch {
    exit(-1)
}
