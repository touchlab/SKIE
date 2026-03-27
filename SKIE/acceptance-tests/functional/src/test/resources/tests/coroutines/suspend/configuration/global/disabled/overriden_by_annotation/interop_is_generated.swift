let a = A()

await Task { @MainActor in
    try await a.foo()
}.cancel()

try! await Task.sleep(1_000_000_000)

exit(a.result)
