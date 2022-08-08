let a: A = A1(value_A1: 1, value_A: 0)

switch exhaustively(a) {
    case .A1(let a):
        exit(a.value_A)
    case .A2(let a):
        exit(1)
}
