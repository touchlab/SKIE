let a: A = A1A(value_A1A: 0, value_A1: 1, value_A: 1)

switch onEnum(of: a) {
    case .A1(let a):
        switch onEnum(of: a) {
            case .A1A(let a):
                exit(a.value_A1A)
            case .A1B(let a):
                exit(1)
        }
    case .A2(let a):
        exit(1)
}
