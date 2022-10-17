let r0 = AKt.bar(i: 0, k: 1)
let r1 = AKt.bar(i: 0)

let r2 = AKt.foo(i: 0, k: 1)
let r3 = AKt.foo(k: 1)

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
