func sum(flow: SkieFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result1 = try! await sum(flow: AKt.foo(A()))
AKt.setFoo(A(), value: AKt.foo2)

let result2 = try! await sum(flow: AKt.foo(A()))

exit(result2 - 2 * result1)
