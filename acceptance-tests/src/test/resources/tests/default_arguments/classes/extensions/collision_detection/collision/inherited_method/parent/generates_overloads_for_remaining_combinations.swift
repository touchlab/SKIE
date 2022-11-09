let b = B()

let r0 = b.foo(i: 0, k: 1)
let r1 = b.foo(k: 1)
let r2 = b.foo()

if r0 == r1 && r1 == r2 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2)")
}
