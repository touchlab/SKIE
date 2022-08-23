# sealed-functionName = exhaustively2

let a: A = A1()

switch exhaustively2(a) {
    case .A1(_):
        exit(0)
    case .A2(_):
        exit(1)
}
