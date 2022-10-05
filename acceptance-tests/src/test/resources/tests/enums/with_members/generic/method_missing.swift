# SwiftCompilationError(value of type 'A' has no member 'foo')

let allValues = [AKt.a1(), AKt.a2()]

for value in allValues {
    value.foo(t: "")
}

exit(0)
