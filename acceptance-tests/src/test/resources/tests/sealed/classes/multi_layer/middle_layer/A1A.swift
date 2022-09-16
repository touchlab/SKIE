let a: A1 = A1A(value_A1A: 1, value_A1: 1, value_A: 1)

switch onEnum(of: a) {
    case .A1A(let a):
        exit(0)
    case .A1B(let a):
        exit(1)
}
