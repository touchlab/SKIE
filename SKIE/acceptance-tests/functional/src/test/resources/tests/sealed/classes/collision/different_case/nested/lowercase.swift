let a: A = A.a1()

switch onEnum(of: a) {
    case .A_A1(_):
        exit(1)
    case .A_a1(_):
        exit(0)
}
