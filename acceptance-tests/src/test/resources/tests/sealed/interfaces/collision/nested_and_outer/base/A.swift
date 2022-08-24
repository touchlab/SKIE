let a: A = AA1()

switch exhaustively(a) {
    case .A1(_):
        exit(0)
    case .A2(_):
        exit(1)
}
