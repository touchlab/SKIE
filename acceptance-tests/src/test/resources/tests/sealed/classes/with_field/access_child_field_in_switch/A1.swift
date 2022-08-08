let a: A = A1(value_A1: 0, value_A: 1)

switch exhaustively(a) {
    case .A1(let a):
        exit(a.value_A1)
    case .A2(let a):
        exit(1)
}
