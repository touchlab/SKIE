let r0 = A<C>(defaultForDefault: C(value: 0), defaultForValue: C(value: 1), value: C(value: 2)).value.value
let r1 = A<C>(defaultForDefault: C(value: 0), defaultForValue: C(value: 1)).value.value + 1
let r2 = A<C>(defaultForDefault: C(value: 0), value: C(value: 2)).value.value
let r3 = A<C>(defaultForDefault: C(value: 0)).value.value + 2

if r0 == r1 && r1 == r2 && r2 == r3 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), r2: \(r2), r3: \(r3)")
}
