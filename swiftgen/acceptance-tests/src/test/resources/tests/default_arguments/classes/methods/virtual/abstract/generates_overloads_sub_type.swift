let b: B = B()

let r0 = b.foo(i: 0, k: 1, m: 2, o: 3)
let r1 = b.foo(i: 0, k: 1, m: 2)
let r2 = b.foo(i: 0, m: 2, o: 3)
let r3 = b.foo(i: 0, m: 2)

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
