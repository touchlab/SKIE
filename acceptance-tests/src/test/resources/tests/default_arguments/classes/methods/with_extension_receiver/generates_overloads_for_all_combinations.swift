let a: A = A()

let b = B()

let r0 = a.foo(b, i: 0, k: 1)
let r1 = a.foo(b, i: 0)
let r2 = a.foo(b, k: 1)
let r3 = a.foo(b)

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
