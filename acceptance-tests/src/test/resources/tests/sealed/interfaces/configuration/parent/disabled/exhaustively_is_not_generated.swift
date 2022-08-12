# SwiftCompilationError(cannot find 'exhaustively' in scope)

func test(a: A) {
    switch exhaustively(a) {
        case .A1(_):
            exit(1)
        case .A2(_):
            exit(0)
    }
}