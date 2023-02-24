func sum(flow: SkieFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result = try! await sum(flow: AKt.foo().first!)

exit(result - 6)
