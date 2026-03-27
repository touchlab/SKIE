let a: A_ = A3()

switch onEnum(of: a) {
    case .a3(_):
        exit(0)
    case .a4(_):
        exit(1)
}
