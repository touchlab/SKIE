let a: A = A1()

switch onEnum(sealed: a) {
    case .a1(_):
        exit(0)
    case .a2(_):
        exit(1)
}
