func sum(flow: SkieSwiftFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result1 = try! await sum(flow: A().foo)
A().foo = AKt.foo2

let result2 = try! await sum(flow: A().foo)

exit(result2 - 2 * result1)
