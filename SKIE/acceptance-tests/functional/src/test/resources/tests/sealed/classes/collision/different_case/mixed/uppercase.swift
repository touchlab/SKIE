let a: A = A.A1()

switch onEnum(of: a) {
    case .A_A1(_):
        exit(0)
    case .a1(_):
        exit(1)
}
