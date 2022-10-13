let a: I = A1(value_A1: 0, value_I: 1, value_K: 1)

switch onEnum(of: a) {
    case .A1(let a):
        exit(a.value_A1)
    case .A2(let a):
        exit(1)
}
