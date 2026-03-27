let a: A = A2.shared

switch onEnum(of: a) {
    case .a1(let a):
        exit(1)
    case .a2(let a):
        exit(a.value_A2)
}
