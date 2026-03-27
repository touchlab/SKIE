# SwiftCompilationError(error: global function 'onEnum(of:)' requires that 'A' inherit from 'Skie_SuspendResult')

func test(a: A) {
    switch onEnum(of: a) {
        case .a1(_):
            exit(1)
        case .a2(_):
            exit(0)
    }
}
