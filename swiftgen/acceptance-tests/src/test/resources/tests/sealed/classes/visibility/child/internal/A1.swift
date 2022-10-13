let a: A = A.Companion.shared.createA1()

switch onEnum(of: a) {
    case .Else:
        exit(0)
    case .A2(_):
        exit(1)
}
