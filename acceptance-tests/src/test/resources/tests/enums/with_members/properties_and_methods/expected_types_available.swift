let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    assert(value.int8Return() == Int8(1))

    switch value {
    case .a1:
        exit(index == 0 ? 0 : 1)
    case .a2:
        exit(index == 1 ? 0 : 1)
    }
}
