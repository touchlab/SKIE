# SwiftCompilationError(switch must be exhaustive)

let a: A = A1()

switch a.exhaustively() {
    case .A1(_):
        exit(0)
}
