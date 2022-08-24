let a: A = A.Companion.shared.createA1()

switch exhaustively(a) {
    case .Else:
        exit(0)
    case .A2(_):
        exit(1)
}
