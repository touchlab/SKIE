# SwiftCompilationError(switch must be exhaustive)

let a: A = A1()

switch onEnum(of: a) {
    case .A1(_):
        exit(0)
}
