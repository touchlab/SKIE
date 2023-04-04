let task = Task.detached {
    for await _ in AKt.flow() {
    }

    return 0
}

try? await Task.sleep(nanoseconds: 1_000_000)

task.cancel()

try? await Task.sleep(nanoseconds: 2_000_000)

exit(AKt.wasCancelled ? 0 : 1)
