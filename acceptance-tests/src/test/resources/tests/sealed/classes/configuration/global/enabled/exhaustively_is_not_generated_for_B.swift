# expected = SwiftCompilationError(cannot convert value of type 'B' to expected argument type 'A') ; sealed-enabled = false

let b: B = B1()

switch exhaustively(b) {
    case .B1(_):
        exit(0)
    case .B2(_):
        exit(1)
}
