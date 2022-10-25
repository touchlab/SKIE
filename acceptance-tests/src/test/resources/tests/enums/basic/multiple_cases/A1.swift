let allValues = [AKt.a1(), AKt.a2(), AKt.a3()]

for (index, value) in allValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    case .a3:
        assert(index == 2)
    }
}

exit(0)
