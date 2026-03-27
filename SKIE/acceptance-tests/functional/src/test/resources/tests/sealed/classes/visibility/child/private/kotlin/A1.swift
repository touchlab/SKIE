let a: A = A.Companion.shared.createA1()

switch onEnum(of: a) {
    case .else:
        exit(0)
    case .a2(_):
        exit(1)
}
