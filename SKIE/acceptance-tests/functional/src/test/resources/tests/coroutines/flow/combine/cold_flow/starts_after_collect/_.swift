var sum: Int32 = 0
var index = 0

let publisher = AKt.flow().toPublisher()

if AKt.emittedElements != 0 {
    exit(-1)
}

let semaphore = DispatchSemaphore(value: 0)

let cancellation = publisher.sink { _ in
    semaphore.signal()
} receiveValue: { value in
    index += 1

    if AKt.emittedElements != value.intValue {
        exit(-AKt.emittedElements)
    }

    sum += value.int32Value
}

withExtendedLifetime(cancellation) {
    semaphore.wait()
}

exit(sum - 6)
