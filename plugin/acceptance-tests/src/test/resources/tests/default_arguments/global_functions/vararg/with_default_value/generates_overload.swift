let r0 = AKt.foo(i: 0, k: KotlinIntArray(size: 1, init: { _ in KotlinInt(1) }))
let r1 = AKt.foo(i: 0)
let r2 = AKt.foo(k: KotlinIntArray(size: 1, init: { _ in KotlinInt(1) }))
let r3 = AKt.foo()

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
