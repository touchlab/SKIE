let a: A = A.A1()

switch onEnum(of: a) {
    case .A1(_):
        exit(1)
    case .A_A1(_):
        exit(0)
    case .A_AA1(_):
        exit(1)
}
