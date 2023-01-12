let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    let b: B = value.b
    let b1: B = value.b1(a: value)

    let __b = b as __B
    let __b1 = b1 as __B

    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

exit(0)
