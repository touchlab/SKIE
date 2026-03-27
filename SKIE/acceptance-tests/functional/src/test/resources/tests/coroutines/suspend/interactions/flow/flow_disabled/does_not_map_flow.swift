let flow: Kotlinx_coroutines_coreFlow = AKt.flow()

let result = try! await AKt.foo(flow: flow).int32Value

exit(result - 6)
