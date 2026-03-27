var sum: Int32 = 0

let outerSemaphore = DispatchSemaphore(value: 0)

let publisher = AKt.flow().toPublisher()

let cancellation = publisher.sink { _ in
    outerSemaphore.signal()
} receiveValue: { i in
    sum += i.int32Value

    let innerSemaphore = DispatchSemaphore(value: 0)

    let cancellation = publisher.sink { _ in
        innerSemaphore.signal()
    } receiveValue: { i in
        sum += i.int32Value
    }

    withExtendedLifetime(cancellation) {
        innerSemaphore.wait()
    }
}

withExtendedLifetime(cancellation) {
    outerSemaphore.wait()
}

exit(sum - 24)
