# Skip

let a = A()

let r0 = a.foo(i: 0)
let r1 = a.foo(k: 1.0)

if r0 == r1 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1)")
}
