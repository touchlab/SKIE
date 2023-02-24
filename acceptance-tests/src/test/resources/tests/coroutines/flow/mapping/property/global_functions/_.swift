func sum(flow: SkieFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result1 = try! await sum(flow: AKt.foo)
AKt.foo = AKt.foo2

let result2 = try! await sum(flow: AKt.foo)

exit(result2 - 2 * result1)
