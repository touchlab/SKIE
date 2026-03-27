let a: A = AKt.a1()
let b1: B = AKt.b1()
let b2: B = a.foo(b: b1)
assert(b1 == b2)

exit(0)
