let a: A = A1()

switch exhaustively(a) {
    case .X(_):
        exit(0)
    case .Other:
        exit(1)
}
