let a: A = A1(i: 0)

switch onEnum(of: a) {
    case .a1(_):
        exit(0)
    case .a2(_):
        exit(1)
}
