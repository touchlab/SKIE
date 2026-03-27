let a = A()

let r0 = await Task.detached {
    try! await A().foo(i: 0, k: 1)
}.value.int32Value

let r1 = await Task.detached {
    try! await A().foo(k: 1)
}.value.int32Value

let r2 = await Task.detached {
    try! await A().foo()
}.value.int32Value

if r0 == r1 && r1 == r2 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2)")
}
