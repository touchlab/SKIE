let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

let b1 = AKt.b(value: Int32(1))
let b2 = A.B(value: Int32(2))

exit(0)
