let a: A = AA1_()

switch onEnum(of: a) {
    case .AA1(_):
        exit(1)
    case .AA1_(_):
        exit(0)
}
