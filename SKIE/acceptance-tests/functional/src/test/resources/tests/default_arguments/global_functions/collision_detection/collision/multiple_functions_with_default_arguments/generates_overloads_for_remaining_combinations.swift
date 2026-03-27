let r0 = AKt.foo(i: 0, k: 1)
let r1 = AKt.foo(k: 1)

let s0 = AKt.foo(i: 0, m: 2.0)
let s1 = AKt.foo(m: 2.0)

if r0 == r1 && s0 == s1 {
    exit(0)
} else {
    fatalError("r0: \(r0), r1: \(r1), s0: \(s0), s1: \(s1)")
}
