let a: A = AImpl()

let r0 = (AKt.foo(a, defaultForDefault: C(value: 0), defaultForReturnValue: C(value: 1), returnValue: C(value: 2)) as! C).value
let r1 = (AKt.foo(a, defaultForDefault: C(value: 0), defaultForReturnValue: C(value: 1)) as! C).value + 1
let r2 = (AKt.foo(a, defaultForDefault: C(value: 0), returnValue: C(value: 2)) as! C).value
let r3 = (AKt.foo(a, defaultForDefault: C(value: 0)) as! C).value + 2

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
