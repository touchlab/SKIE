let allValues = [AKt.b1(), AKt.b2()]

for (index, value) in allValues.enumerated() {
    switch value {
    case .b1:
        assert(index == 0)
    case .b2:
        assert(index == 1)
    }
}

exit(0)
