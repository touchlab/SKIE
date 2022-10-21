let a = A()

let result = try! await a.foo()

exit(result.int32Value)
