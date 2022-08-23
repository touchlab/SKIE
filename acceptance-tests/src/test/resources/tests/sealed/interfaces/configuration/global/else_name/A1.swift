# sealed-elseName = Other

let a: A = A1(i: 0)

switch exhaustively(a) {
    case .Other:
        exit(0)
    case .A2(_):
        exit(1)
}
