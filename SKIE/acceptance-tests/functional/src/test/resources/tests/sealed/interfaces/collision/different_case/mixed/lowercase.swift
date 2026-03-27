let a: A = a1()

switch onEnum(of: a) {
    case .AA1(_):
        exit(1)
    case .a1(_):
        exit(0)
}
