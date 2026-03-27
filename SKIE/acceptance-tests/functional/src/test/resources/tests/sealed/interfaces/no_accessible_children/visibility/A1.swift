let a: A = ACompanion.shared.createA1()

switch onEnum(of: a) {
    case .else:
        exit(0)
}
