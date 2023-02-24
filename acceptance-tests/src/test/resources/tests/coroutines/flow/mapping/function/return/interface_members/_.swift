func sum(flow: SkieFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result = try! await sum(flow: A().foo())

exit(result - 6)
