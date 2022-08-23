# expected = SwiftCompilationError(switch must be exhaustive)

let a: A = A1()

switch exhaustively(a) {
    case .A1(_):
        exit(0)
}
