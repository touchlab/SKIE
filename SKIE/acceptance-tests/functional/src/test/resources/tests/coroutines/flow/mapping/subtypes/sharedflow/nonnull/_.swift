func sum(flow: SkieSwiftSharedFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: SkieSwiftFlow(flow), max: 2).int32Value
}

let flow = AKt.flow

try! await Task.sleep(nanoseconds: 1_000_000_000)

let resultSum = try! await sum(flow: flow)
let cacheSum = flow.replayCache.map { $0.intValue }.reduce(0, +)

if resultSum == 5 && cacheSum == 5 {
    exit(0)
} else {
    print("resultSum: \(resultSum), cacheSum: \(cacheSum)")
    exit(1)
}
