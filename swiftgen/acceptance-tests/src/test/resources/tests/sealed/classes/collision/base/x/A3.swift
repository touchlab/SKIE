let a: A_ = A3()

switch onEnum(of: a) {
    case .A3(_):
        exit(0)
    case .A4(_):
        exit(1)
}
