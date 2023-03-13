func sum(flow: SkieSwiftMutableStateFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: SkieSwiftFlow(flow), max: 1).int32Value
}

try! await AKt.flow.emit(value: KotlinInt(1))

let sum1 = AKt.flow.value

AKt.flow.value = KotlinInt(2)

let sum2 = try! await sum(flow: AKt.flow)

let emitResult = AKt.flow.tryEmit(value: KotlinInt(3))

let sum3 = try! await sum(flow: AKt.flow)

let cacheSum = AKt.flow.replayCache.map { $0.intValue }.reduce(0, +)

let subscriptionCount = AKt.flow.subscriptionCount.value

let setResult = AKt.flow.compareAndSet(expect: 3, update: 4)

let sum4 = AKt.flow.value

if sum1 == 1 && sum2 == 2 && sum3 == 3 && sum4 == 4 && cacheSum == 3 && subscriptionCount == 0 && emitResult && setResult {
    exit(0)
}
