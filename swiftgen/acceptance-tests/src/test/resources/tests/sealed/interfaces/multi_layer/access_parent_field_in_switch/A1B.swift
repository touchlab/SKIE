let a: A = A1B(value_A1B: 1, value_A1: 0, value_A: 1)

switch onEnum(of: a) {
    case .A1(let a):
        switch onEnum(of: a) {
            case .A1A(let a):
                exit(1)
            case .A1B(let a):
                exit(a.value_A1)
        }
    case .A2(let a):
        exit(1)
}
