let b = B()

let r0 = b.foo(i: 0)
let r1 = b.foo(k: 1.0)

if r0 == r1 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1)")
}
