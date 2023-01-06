let task = Task {
    try await (A1() as A).foo()
}

do {
   try await task.value
} catch is CancellationError {
    exit(0)
} catch {
    exit(-1)
}
