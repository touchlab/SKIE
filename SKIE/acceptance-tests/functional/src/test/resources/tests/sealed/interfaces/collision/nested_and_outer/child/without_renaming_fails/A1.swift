let a: A = AA1()

switch onEnum(of: a) {
    case .A1(_):
        exit(1)
    case .AA1(_):
        exit(0)
    case .AAA1(_):
        exit(1)
}
