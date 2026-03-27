import Combine

// Run 10 times to reduce the probabilty of flakiness.
(1...10).forEach { index in
    let receiveValueSemaphore = DispatchSemaphore(value: 0)
    let insideReceiveValueSemaphore = DispatchSemaphore(value: 0)
    let receiveCancelSemaphore = DispatchSemaphore(value: 0)

    var receivedValues: [Int32] = []
    let a = A()
    let cancellation = a.flow().toPublisher()
        .handleEvents(
            receiveCompletion: { completion in
                exit(2)
            },
            receiveCancel: {
                receiveCancelSemaphore.signal()
            }
        )
        .sink { completion in
            exit(3)
        } receiveValue: { value in
            receivedValues.append(value.int32Value)
            receiveValueSemaphore.signal()

            insideReceiveValueSemaphore.wait()
        }

    assert(receiveValueSemaphore.wait(wallTimeout: .now() + .seconds(1)) == .success, "[\(index)] Timed out waiting for first value")
    cancellation.cancel()
    insideReceiveValueSemaphore.signal()

    assert(receiveCancelSemaphore.wait(wallTimeout: .now() + .seconds(1)) == .success, "[\(index)] Timed out waiting for cancellation")

    assert(receiveValueSemaphore.wait(wallTimeout: .now() + .seconds(1)) == .timedOut, "[\(index)] Received another value unexpectedly")

    assert(receivedValues == [0], "[\(index)] Expected [0], received: \(receivedValues)")

    assert(a.lastEmitted == nil || a.lastEmitted == 0, "[\(index)] Expected lastEmitted to be nil or 0, was \(a.lastEmitted)")
    assert(a.wasCancelled, "[\(index)] Expected wasCancelled to be true, was false")
}

exit(0)
