# SwiftCompilationError(error: no exact matches in call to global function 'onEnum')

let b: B = B1()

switch onEnum(of: b) {
    case .B1(_):
        exit(0)
    case .B2(_):
        exit(1)
}
