let a: A = A1(value_A1: 1, value_A: 0)

switch onEnum(of: a) {
    case .a1(let a):
        exit(a.value_A)
    case .a2(let a):
        exit(1)
}
