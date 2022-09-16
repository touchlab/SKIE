let a: A = ACompanion.shared.createA1()

switch onEnum(of: a) {
    case .Else:
        exit(0)
    case .A2(_):
        exit(1)
}
