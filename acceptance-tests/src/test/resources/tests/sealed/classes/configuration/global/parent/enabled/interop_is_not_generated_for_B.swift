# SwiftCompilationError(error: global function 'onEnum(of:)' requires that 'B' inherit from 'A')

let b: B = B1()

switch onEnum(of: b) {
    case .B1(_):
        exit(0)
    case .B2(_):
        exit(1)
}
