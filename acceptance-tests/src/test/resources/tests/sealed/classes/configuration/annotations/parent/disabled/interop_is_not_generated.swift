# SwiftCompilationError(error: global function 'onEnum(of:)' requires that 'A' inherit from 'Skie_SuspendResult')

func test(a: A) {
    switch onEnum(of: a) {
        case .A1(_):
            exit(1)
        case .A2(_):
            exit(0)
    }
}
