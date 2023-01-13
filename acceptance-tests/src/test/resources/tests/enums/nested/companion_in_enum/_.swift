let allValues = [AKt.a1(), AKt.a2()]

for (index, value) in allValues.enumerated() {
    switch value {
    case .a1:
        assert(index == 0)
    case .a2:
        assert(index == 1)
    }
}

let companion1 = AKt.companion()
let companion2 = A.Companion.shared

exit(0)
