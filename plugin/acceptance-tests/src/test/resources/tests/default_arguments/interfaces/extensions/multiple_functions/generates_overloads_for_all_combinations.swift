let a: A = AImpl()

let r0 = AKt.bar(a, i: 0, k: 1)
let r1 = AKt.bar(a, i: 0)

let r2 = AKt.foo(a, i: 0, k: 1)
let r3 = AKt.foo(a, k: 1)

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
