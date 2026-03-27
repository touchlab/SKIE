func sum(flow: SkieSwiftOptionalMutableStateFlow<KotlinInt>) async throws -> Int32 {
    return flow.value?.int32Value ?? 0
}

try! await AKt.flow.emit(value: KotlinInt(1))

let sum1 = AKt.flow.value?.int32Value ?? 0

try! await AKt.flow.emit(value: nil)

let sum0 = try! await sum(flow: AKt.flow)

AKt.flow.value = KotlinInt(2)

let sum2 = try! await sum(flow: AKt.flow)

let emitResult = AKt.flow.tryEmit(value: KotlinInt(3))

let sum3 = try! await sum(flow: AKt.flow)

let emitResult2 = AKt.flow.tryEmit(value: nil)

let cacheSum = AKt.flow.replayCache.map { $0?.intValue ?? 0 }.reduce(0, +)

let subscriptionCount = AKt.flow.subscriptionCount.value

let setResult = AKt.flow.compareAndSet(expect: nil, update: 4)

let sum4 = AKt.flow.value?.int32Value ?? 0

AKt.flow.value = nil

let value0 = AKt.flow.value?.int32Value ?? 0

AKt.flow.value = 0

let setResult2 = AKt.flow.compareAndSet(expect: 0, update: 5)

let value5 = AKt.flow.value?.int32Value ?? 0

if sum1 != 1 {
    exit(1)
}

if sum0 != 0 {
    exit(2)
}

if sum2 != 2 {
    exit(3)
}

if sum3 != 3 {
    exit(4)
}

if sum4 != 4 {
    exit(5)
}

if value0 != 0 {
    exit(6)
}

if value5 != 5 {
    exit(7)
}

if cacheSum != 0 {
    exit(8)
}

if subscriptionCount != 0 {
    exit(9)
}

if !emitResult {
    exit(10)
}

if !emitResult2 {
    exit(11)
}

if !setResult {
    exit(12)
}

if !setResult2 {
    exit(13)
}

exit(0)
