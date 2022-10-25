let aValues = [AKt.a1(), AKt.a2()]
let bValues = [AKt.b1(), AKt.b2()]

for (index, value) in aValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

for (index, value) in bValues.enumerated() {
    switch value {
    case .b1:
        assert(index == 0)
    case .b2:
        assert(index == 1)
    }
}

exit(0)
