let a = BaseA()

let r0 = a.foo(i: 0, k: 1)
let r1 = a.foo(i: 0)
let r2 = a.foo(k: 1)
let r3 = a.foo()

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
