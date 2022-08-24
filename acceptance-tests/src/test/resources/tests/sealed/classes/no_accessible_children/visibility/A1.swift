let a: A = A.Companion.shared.createA1()

switch exhaustively(a) {
    case .Else:
        exit(0)
}
