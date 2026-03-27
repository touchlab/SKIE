var sum: Int32 = 0

var receivedValues: [Int32] = []

let receiveValueSemaphore = DispatchSemaphore(value: 0)
let receiveCancellationErrorCompletionSemaphore = DispatchSemaphore(value: 0)

let cancellation = AKt.flow().toPublisher()
    .handleEvents(
        receiveCancel: {
            exit(4)
        }
    )
    .sink { completion in
        if case .failure(let error) = completion {
            if error is CancellationError {
                receiveCancellationErrorCompletionSemaphore.signal()
            } else {
                exit(3)
            }
        } else {
            exit(2)
        }
    } receiveValue: { value in
        receivedValues.append(value.int32Value)
        sum += value.int32Value

        receiveValueSemaphore.signal()
    }

withExtendedLifetime(cancellation) {
    assert(receiveValueSemaphore.wait(wallTimeout: .now() + .seconds(2)) == .success, "Timed out waiting for first value")

    assert(receiveValueSemaphore.wait(wallTimeout: .now() + .seconds(2)) == .success, "Timed out waiting for second value")

    assert(receiveValueSemaphore.wait(wallTimeout: .now() + .seconds(2)) == .success, "Timed out waiting for third value")


    assert(receiveCancellationErrorCompletionSemaphore.wait(wallTimeout: .now() + .seconds(2)) == .success, "Timed out waiting for CancellationError completion")

    assert(receiveValueSemaphore.wait(wallTimeout: .now() + .seconds(2)) == .timedOut, "Received another value unexpectedly")
}

assert(receivedValues == [1, 2, 3])

exit(sum - 6)
