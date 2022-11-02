let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

let b = AKt.b()

exit(0)
