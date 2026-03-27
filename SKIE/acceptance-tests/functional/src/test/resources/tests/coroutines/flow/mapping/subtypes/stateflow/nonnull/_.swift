func sum(flow: SkieSwiftStateFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: SkieSwiftFlow(flow), max: 1).int32Value
}

let flow = AKt.flow

try! await Task.sleep(nanoseconds: 1_000_000_000)

let resultSum = try! await sum(flow: flow)
let cacheSum = flow.replayCache.map { $0.intValue }.reduce(0, +)

if resultSum == 1 && AKt.flow.value == 1 && cacheSum == 1 {
    exit(0)
}
