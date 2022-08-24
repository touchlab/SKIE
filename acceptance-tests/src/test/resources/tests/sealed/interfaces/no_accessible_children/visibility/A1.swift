let a: A = ACompanion.shared.createA1()

switch exhaustively(a) {
    case .Else:
        exit(0)
}
