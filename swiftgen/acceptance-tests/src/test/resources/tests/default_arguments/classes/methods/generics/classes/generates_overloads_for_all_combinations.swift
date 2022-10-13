let a = A<C>(defaultForDefault: C(value: 0))

let r0 = a.foo(defaultForReturnValue: C(value: 1), returnValue: C(value: 2)).value
let r1 = a.foo(defaultForReturnValue: C(value: 1)).value + 1
let r2 = a.foo(returnValue: C(value: 2)).value
let r3 = a.foo().value + 2

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
