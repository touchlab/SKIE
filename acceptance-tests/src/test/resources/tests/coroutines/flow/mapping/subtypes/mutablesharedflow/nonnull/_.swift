func sum(flow: SkieSwiftMutableSharedFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: SkieSwiftFlow(flow), max: 1).int32Value
}

try! await AKt.flow.emit(value: KotlinInt(1))

let sum1 = try! await sum(flow: AKt.flow)

try! await AKt.flow.emit(value: KotlinInt(2))

let sum2 = try! await sum(flow: AKt.flow)

AKt.flow.tryEmit(value: KotlinInt(3))

let sum3 = try! await sum(flow: AKt.flow)

let cacheSum = AKt.flow.replayCache.map { $0.intValue }.reduce(0, +)

AKt.flow.resetReplayCache()

let cacheSumAfterReset = AKt.flow.replayCache.map { $0.intValue }.reduce(0, +)

let subscriptionCount = AKt.flow.subscriptionCount.value

if sum1 == 1 && sum2 == 2 && sum3 == 3 && cacheSum == 3 && cacheSumAfterReset == 0 && subscriptionCount == 0 {
    exit(0)
}
