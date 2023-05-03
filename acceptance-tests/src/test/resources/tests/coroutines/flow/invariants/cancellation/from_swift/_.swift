actor Signal {
    private(set) var wasSignaled = false

    func sendSignal() {
        wasSignaled = true
    }
}

let signal = Signal()

let task = Task.detached {
    for await _ in AKt.flow() {
        await signal.sendSignal()
    }

    return 0
}

while await !signal.wasSignaled {
    try? await Task.sleep(nanoseconds: 1_000_000)
}

task.cancel()

try? await Task.sleep(nanoseconds: 10_000_000_000)

exit(AKt.wasCancelled ? 0 : 1)
