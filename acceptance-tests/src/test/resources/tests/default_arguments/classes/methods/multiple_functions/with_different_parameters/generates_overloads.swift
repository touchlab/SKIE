let a: A = A()

let r0 = a.foo(i: "0", k: 2)
let r1 = a.foo(i: "0")
let r2 = a.foo(k: 2)

let r3 = a.foo(a: 1, b: 0.0)
let r4 = a.foo(b: 0.0)
let r5 = a.foo(a: 1)

if (r0 == r1 && r1 == r2) && (r3 == r4 && r4 == r5) {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3), r4: \(r4), r5: \(r5)")
}
