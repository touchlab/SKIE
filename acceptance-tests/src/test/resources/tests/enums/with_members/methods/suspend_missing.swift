# SwiftCompilationError(value of type 'A' has no member 'suspendingNoParam')

let allValues = [AKt.a1(), AKt.a2()]

for value in allValues {
    value.suspendingNoParam()
}

exit(0)
