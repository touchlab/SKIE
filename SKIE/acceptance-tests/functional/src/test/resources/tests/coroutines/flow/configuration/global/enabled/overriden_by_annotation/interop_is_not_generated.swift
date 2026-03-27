# SwiftCompilationError(error: cannot convert value of type 'any Kotlinx_coroutines_coreFlow' to expected argument type 'SkieSwiftFlow<KotlinInt>')

func sum(flow: SkieSwiftFlow<KotlinInt>) async throws -> Int32 {
    return try await SumKt.sum(flow: flow).int32Value
}

let result = try! await sum(flow: AKt.foo())

exit(result - 6)
