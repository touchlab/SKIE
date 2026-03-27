let a: A = AImpl()

let result = try! await AKt.foo(a)

exit(result.int32Value)
