let a: A = ACompanion.shared.createA1()

switch onEnum(of: a) {
    case .else:
        exit(0)
    case .a2(_):
        exit(1)
}
