let allValues = [AKt.a1(), AKt.a2()]

for value in allValues {
    try! await value.suspendingNoParam()
}

exit(0)
