# RuntimeError(Uncaught Kotlin exception: kotlin.IllegalStateException: Exception from Kotlin)

let publisher = AKt.foo().toPublisher()

let semaphore = DispatchSemaphore(value: 0)

let cancellation = publisher.sink { _ in
    semaphore.signal()
} receiveValue: { value in
    if value != 1 {
        exit(1)
    }
}

withExtendedLifetime(cancellation) {
    semaphore.wait()
}

exit(2)
