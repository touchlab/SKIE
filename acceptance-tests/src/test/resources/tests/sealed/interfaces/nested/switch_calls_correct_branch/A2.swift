let a: A = AA2()

switch exhaustively(a) {
    case .A1(_):
        exit(1)
    case .A2(_):
        exit(0)
}
