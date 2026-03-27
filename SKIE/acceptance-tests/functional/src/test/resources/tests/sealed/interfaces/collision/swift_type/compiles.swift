let a: A = AA1()

switch onEnum(of: a) {
    case .a1(_):
        exit(0)
    case .type(_):
        exit(1)
}
