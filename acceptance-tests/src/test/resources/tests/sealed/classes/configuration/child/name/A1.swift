let a: A = A1(i: 0)

switch exhaustively(a) {
    case .A3(let a1):
        exit(a1.i)
    case .A2(_):
        exit(1)
}
