func sum(flow: SkieSwiftFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result = try! await sum(flow: SkieSwiftFlow(AKt.foo().value!.value!))

exit(result - 6)
