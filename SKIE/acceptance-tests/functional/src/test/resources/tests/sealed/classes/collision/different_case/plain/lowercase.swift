let a: A = a1()

switch onEnum(of: a) {
    case .A1(_):
        exit(1)
    case .a1(_):
        exit(0)
}
