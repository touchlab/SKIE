let a: A = AA1()

switch onEnum(of: a) {
    case .AA1(_):
        exit(0)
    case .a1(_):
        exit(1)
}
