let a: A = A()

let r0 = a.foo(i: 0, k: 1, m: 2)
let r1 = a.foo(i: 0, k: 1)
let r2 = a.foo(i: 0, m: 2)
let r3 = a.foo(i: 0)
let r4 = a.foo(k: 1, m: 2)
let r5 = a.foo(k: 1)
let r6 = a.foo(m: 2)
let r7 = a.foo()

if r0 == r1 && r1 == r2 && r2 == r3 && r3 == r4 && r4 == r5 && r5 == r6 && r6 == r7 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3), r4: \(r4), r5: \(r5), r6: \(r6), r7: \(r7)")
}