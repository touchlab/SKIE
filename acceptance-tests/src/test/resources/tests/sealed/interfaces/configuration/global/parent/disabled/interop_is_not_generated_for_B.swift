# SwiftCompilationError(argument type 'any B' does not conform to expected type 'A')

let b: B = B1()

switch onEnum(of: b) {
    case .B1(_):
        exit(0)
    case .B2(_):
        exit(1)
}
