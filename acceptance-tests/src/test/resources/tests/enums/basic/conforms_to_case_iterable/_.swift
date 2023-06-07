for (index, value) in A.allCases.enumerated() {
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
