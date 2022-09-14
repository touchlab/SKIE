let a: A = AKt.randomA()

assert(a.foo() == 0)
assert(a.bar(param: 10) == 10)

switch a {
case .a1:
    exit(0)
case .a2:
    exit(0)
}
