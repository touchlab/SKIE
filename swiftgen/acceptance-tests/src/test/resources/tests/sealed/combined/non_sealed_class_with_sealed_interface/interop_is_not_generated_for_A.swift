# SwiftCompilationError(argument type 'A' does not conform to expected type 'I')

func test(a: A) {
    switch onEnum(of: a) {
        case .A1(_):
            exit(1)
        case .A2(_):
            exit(0)
    }
}