let a: A = AImpl<C>(defaultForDefault: C(value: 0))

let r0 = AKt.foo(a, defaultForReturnValue: C(value: 1), returnValue: C(value: 2)).value
let r1 = AKt.foo(a, defaultForReturnValue: C(value: 1)).value + 1
let r2 = AKt.foo(a, returnValue: C(value: 2)).value
let r3 = AKt.foo(a).value + 2

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
