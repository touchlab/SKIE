let a: A = A1(i: 0)

switch onEnum(of: a) {
    case .else:
        exit(0)
    case .a2(_):
        exit(1)
}
