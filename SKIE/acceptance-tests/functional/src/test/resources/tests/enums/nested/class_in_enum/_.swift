let allValues: [A] = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

let b1: A.B = AKt.b()
let b2: A.B = A.B()

exit(0)
