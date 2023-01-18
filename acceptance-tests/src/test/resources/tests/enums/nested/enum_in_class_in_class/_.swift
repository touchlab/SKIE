let allValues = [AKt.c1(), AKt.c2()]

for (index, value) in allValues.enumerated() {
    switch value {
    case .c1:
        assert(index == 0)
    case .c2:
        assert(index == 1)
    }
}

exit(0)
