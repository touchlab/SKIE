let a = BaseB()

let r0 = a.foo(i: 0, k: 1)
let r2 = a.foo(k: 1)
let r3 = a.foo()

if r0 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r2: \(r2), r3: \(r3)")
}
