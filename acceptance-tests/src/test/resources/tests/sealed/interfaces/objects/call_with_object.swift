let a: A = A2.shared

switch onEnum(of: a) {
    case .A1(let a):
        exit(1)
    case .A2(let a):
        exit(a.value_A2)
}
