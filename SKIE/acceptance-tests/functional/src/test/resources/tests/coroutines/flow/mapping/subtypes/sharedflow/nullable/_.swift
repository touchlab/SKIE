func sum(flow: SkieSwiftOptionalSharedFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: SkieSwiftOptionalFlow(flow), max: 2).int32Value
}

let flow = AKt.flow

try! await Task.sleep(nanoseconds: 1_000_000_000)

let resultSum = try! await sum(flow: flow)
let cacheSum = flow.replayCache.map { $0?.intValue ?? 0 }.reduce(0, +)

if resultSum == 3 && cacheSum == 3 {
    exit(0)
}
