func sum(flow: SkieSwiftSharedFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: SkieSwiftFlow(flow), max: 2).int32Value
}

let resultSum = try! await sum(flow: AKt.flow)
let cacheSum = AKt.flow.replayCache.map { $0.intValue }.reduce(0, +)

if resultSum == 5 && cacheSum == 5 {
    exit(0)
}
