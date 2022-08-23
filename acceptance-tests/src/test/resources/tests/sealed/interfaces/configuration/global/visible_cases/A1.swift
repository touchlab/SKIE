# sealed-visibleCases = false

let a: A = A1(i: 0)

switch exhaustively(a) {
    case .Else:
        exit(0)
    case .A2(_):
        exit(1)
}
