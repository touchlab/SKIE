# SwiftCompilationError(candidate requires that 'A' conform to 'I' (requirement specified as '__Sealed' : 'I'))

func test(a: A) {
    switch onEnum(of: a) {
        case .a1(_):
            exit(1)
        case .a2(_):
            exit(0)
    }
}
