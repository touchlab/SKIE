func sum(flow: SkieSwiftOptionalMutableSharedFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: SkieSwiftOptionalFlow(flow), max: 1).int32Value
}

try! await AKt.flow.emit(value: KotlinInt(1))

let sum1 = try! await sum(flow: AKt.flow)

try! await AKt.flow.emit(value: nil)

let sum0 = try! await sum(flow: AKt.flow)

let emitResult = AKt.flow.tryEmit(value: KotlinInt(3))

let sum3 = try! await sum(flow: AKt.flow)

let emitResult2 = AKt.flow.tryEmit(value: nil)

let cacheSum = AKt.flow.replayCache.map { $0?.intValue ?? 0 }.reduce(0, +)

AKt.flow.resetReplayCache()

let cacheSumAfterReset = AKt.flow.replayCache.map { $0?.intValue ?? 0 }.reduce(0, +)

let subscriptionCount = AKt.flow.subscriptionCount.value

if sum1 == 1 && sum0 == 0 && sum3 == 3 && cacheSum == 0 && cacheSumAfterReset == 0 && subscriptionCount == 0 && emitResult && emitResult2 {
    exit(0)
}
