func sum(flow: SkieSwiftOptionalFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result = try! await sum(flow: A<KotlinInt>().foo())

exit(result - 6)
