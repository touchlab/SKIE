let a: A = AKt.randomA()

assert(a.foo == 0)
assert(a.bar == "Hello world")
a.bar = "Hello world 2"
assert(a.bar == "Hello world 2")

switch a {
case .a1:
    exit(0)
case .a2:
    exit(0)
}
