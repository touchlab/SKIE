let aValues = [AKt.a1(), AKt.a2()]
let aaValues = [AKt.aa1(), AKt.aa2()]

for (index, value) in aValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

for (index, value) in aaValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

exit(0)
